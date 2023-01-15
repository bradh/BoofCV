/*
 * Copyright (c) 2022, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.android.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ninox360.data.CameraSettingsBoof
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * Camera fragment that creates ImageReader for processing camera images but doesn't do any
 * processing or thread management. Provides most of the boilerplate for constructing an image
 */
abstract class CameraProcessFragment : Fragment() {
    // Contains how it should configure the camera. Used to communicate between fragments
    protected val cameraSettings: CameraSettingsBoof by activityViewModels()

    /** Used to adjust how it captures images. Affects quality and speed */
    private var captureRequestTemplateType = CameraDevice.TEMPLATE_RECORD

    /** Data structures for each camera and capture surface */
    protected val cameraDevices = HashMap<String, DeviceSurfaces>()

    /** This is called after the camera has initialized */
    protected var invokeAfterCameraInitialize = {}

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    protected val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    protected val characteristics: CameraCharacteristics
            by lazy { cameraManager.getCameraCharacteristics(cameraSettings.cameraID.value!!.id) }

    /** [HandlerThread] where all camera operations run */
    private val cameraThread = HandlerThread("CameraHandler").apply { start() }

    /** [Handler] corresponding to [cameraThread] */
    private val cameraHandler = Handler(cameraThread.looper)

    override fun onDestroyView() {
        for (cam in cameraDevices.values) {
            cam.session?.stopRepeating()
            cam.device.close()
            for (reader in cam.readers) {
                reader.close()
            }
        }
        cameraDevices.clear()
        super.onDestroyView()
    }

    /**
     * Adds a surface for the camera device which will invoke the following operator every
     * time a new image is captured. Blocks until finished.
     */
    protected fun addCameraProcessor(
        deviceID: String,
        width: Int,
        height: Int,
        op: (image: Image) -> Unit
    ): ImageReader {
        val resolution = selectResolution(deviceID, width, height)

        val imageReader = ImageReader.newInstance(
            resolution.width, resolution.height, imageFormat, IMAGE_BUFFER_SIZE
        )
        imageReader.setOnImageAvailableListener(
            createOnAvailableListener(op),
            cameraHandler
        )

        val camera = lookupDeviceSurfaces(deviceID)
        camera.readers.add(imageReader)
        camera.surfaces.add(imageReader.surface)
        return imageReader
    }

    /**
     * Adds a new surface for the device. Blocks until finished.
     */
    protected fun addCameraSurface(deviceID: String, surface: Surface) {
        Log.i(TAG, "camera: add surface: id=$deviceID")
        val camera: DeviceSurfaces = lookupDeviceSurfaces(deviceID)
        camera.surfaces.add(surface)
    }

    /**
     * Looks up the device and opens the device if it has not already been opened while
     * creating a new [DeviceSurfaces]
     */
    private fun lookupDeviceSurfaces(deviceID: String): DeviceSurfaces {
        val camera: DeviceSurfaces
        if (deviceID in cameraDevices) {
            camera = cameraDevices[deviceID]!!
        } else {
            camera = DeviceSurfaces(openCamera(deviceID))
            cameraDevices[deviceID] = camera
            Log.i(TAG, "added new surface. id=${deviceID}")
        }
        return camera
    }

    /**
     * Call after configuring cameras.
     */
    protected fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        Log.i(TAG, "Inside initializeCamera")

        if (cameraDevices.isEmpty())
            throw RuntimeException("Need to add camera surfaces first")

        for (camera in cameraDevices.values) {
            val requestBuilder = camera.device.createCaptureRequest(captureRequestTemplateType)
            for (surface in camera.surfaces) {
                requestBuilder.addTarget(surface)
            }

            val cameraZoom = cameraSettings.cameraZoom.value!!
            setZoom(cameraZoom, requestBuilder)

            val session = createCaptureSession(camera)
            session.setRepeatingRequest(
                requestBuilder.build(),
                createCaptureRequestListener(camera),
                cameraHandler
            )

            camera.session = session
        }

        invokeAfterCameraInitialize.invoke()
    }

    /**
     * Used to provide a capture request listener. By default null is returned.
     */
    open fun createCaptureRequestListener(camera: DeviceSurfaces): CameraCaptureSession.CaptureCallback? {
        return null
    }

    /**
     * Changes the camera's zoom level. With a Pixel 6 Pro this will be done by down sampling
     * the image less up to 2x zoom, after that it will be a digital zoom.
     */
    private fun setZoom(zoomValue: Double, builder: CaptureRequest.Builder) {
        val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!
        val width = sensorSize.width()
        val height = sensorSize.height()
        val centerX = width / 2
        val centerY = height / 2
        val deltaX = (width * 0.5 / zoomValue).roundToInt()
        val deltaY = (height * 0.5 / zoomValue).roundToInt()

        val cropRegion =
            Rect(centerX - deltaX, centerY - deltaY, centerX + deltaX, centerY + deltaY)
        builder.set(CaptureRequest.SCALER_CROP_REGION, cropRegion)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(cameraID: String): CameraDevice {
        val results = OpenResult()
        cameraManager.openCamera(cameraID, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.i(TAG, "Camera $cameraID has opened")
                results.camera = camera
                results.finished = true
            }

            override fun onDisconnected(camera: CameraDevice) {
                Log.w(TAG, "Camera $cameraID has been disconnected")
                requireActivity().finish()
                results.finished = true
            }

            override fun onError(camera: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraID error: ($error) $msg")
                Log.e(TAG, exc.message, exc)
                results.finished = true
            }
        }, cameraHandler)

        // If there's a coroutine way to do this we should do this. The problem with
        // suspendCancelableCoroutine is that it didn't lend itself towards configuring
        // multiple cameras / surfaces by calling functions to add them. It would load
        // the first camera, suspend then go to the next function before it initialized
        // the first camera. Causing bad stuff
        val startTime = System.currentTimeMillis()
        while (!results.finished && System.currentTimeMillis() < startTime + 10_000L) {
            Thread.yield()
        }
        return results.camera!!
    }

    /**
     * Starts a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine
     */
    private suspend fun createCaptureSession(camera: DeviceSurfaces):
            CameraCaptureSession = suspendCoroutine { cont ->

        val deviceID = cameraSettings.cameraID.value!!

        // Configure it so it can point a camera inside a multi-camera system
        val configurations = ArrayList<OutputConfiguration>()
        for (surface in camera.surfaces) {
            val config = OutputConfiguration(surface)
            if (!deviceID.isLogical) {
                config.setPhysicalCameraId(deviceID.id)
            }
            configurations.add(config)
        }

        // Create a capture session using the predefined targets; this also involves defining the
        // session state callback to be notified of when the session is ready
        camera.device.createCaptureSessionByOutputConfigurations(
            configurations,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    val exc = RuntimeException("Camera $deviceID session configuration failed")
                    Log.e(TAG, exc.message, exc)
                    cont.resumeWithException(exc)
                }
            },
            cameraHandler
        )
    }

    /**
     * Handle new images from the camera when they are available
     */
    private fun createOnAvailableListener(op: (image: Image) -> Unit): ImageReader.OnImageAvailableListener {
        return ImageReader.OnImageAvailableListener { reader ->
            reader.acquireNextImage().use { dataFrame ->
                // This can happen if the process is closed out of order
                if (dataFrame == null) {
                    Log.e(TAG, "null dataFrame")
                    return@use
                }
                // Catch the issue here to prevent it from becomming a big issue
                try {
                    op.invoke(dataFrame)
                } catch (e: Throwable) {
                    Log.e(TAG, "Exception in acquireNextImage", e)
                }
            }
        }
    }

    /**
     * Selects the resolution which has the closest number of pixels to the target
     */
    fun selectResolution(id: String, targetWidth: Int, targetHeight: Int): Size {
        // Search to find best match
        val characteristics = cameraManager.getCameraCharacteristics(id)
        val map = characteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
        val sizes = map!!.getOutputSizes(imageFormat)

        var bestSize = sizes[0]
        var bestError = sizeError(bestSize, targetWidth, targetHeight)
        for (i in 1 until sizes.size) {
            val error = sizeError(sizes[i], targetWidth, targetHeight)
            if (error >= bestError)
                continue
            bestError = error
            bestSize = sizes[i]
        }

        Log.i(
            TAG,
            "resolution: id='$id' target={${targetWidth}x${targetHeight}} selected={${bestSize.width}x${bestSize.height}}"
        )

        return bestSize
    }

    /** Computes how different the passes in size is from the requested image size */
    private fun sizeError(size: Size, targetWidth: Int, targetHeight: Int): Int {
        return abs(size.width - targetWidth) + abs(size.height - targetHeight)
    }

    /** Concise summary of camera settings as a string */
    fun cameraSettingsString(useVision:Boolean): String {
        val deviceID = cameraSettings.cameraID.value!!
        val cameraZoom = cameraSettings.cameraZoom.value!!
        val cameraWidth = (if (useVision) cameraSettings.visionWidth else cameraSettings.width).value!!
        val cameraHeight =(if (useVision) cameraSettings.visionHeight else cameraSettings.height).value!!
        return "C: %s Z: %.1f R: %dx%d".format(deviceID.id, cameraZoom, cameraWidth, cameraHeight)
    }

    /**
     * Camera device, it's surfaces, and image readers
     */
    inner class DeviceSurfaces(val device: CameraDevice) {
        val surfaces = ArrayList<Surface>()
        val readers = ArrayList<ImageReader>()
        var session: CameraCaptureSession? = null
    }

    /**
     * Keeps track of asynchronous state when opening a camera
     */
    inner class OpenResult {
        var exception: Exception? = null
        var camera: CameraDevice? = null
        var finished = false
    }

    companion object {
        private const val TAG = "CameraProcFrag"

        /** Maximum number of images that will be held in the reader's buffer */
        private const val IMAGE_BUFFER_SIZE: Int = 3

        private const val imageFormat = ImageFormat.YUV_420_888
    }
}