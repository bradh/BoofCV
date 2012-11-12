/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
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

package boofcv.abst.feature.detect.intensity;

import boofcv.struct.QueueCorner;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;

/**
 * Extracts corners from a the image and or its gradient.  This is a generalized interface and lacks some of the functionality
 * of more specialized classes.
 *
 * @see boofcv.alg.feature.detect.intensity
 * @see boofcv.abst.feature.detect.extract
 *
 * @param <I> Input image type.
 * @param <D> Image derivative type.
 *
 * @author Peter Abeles
 */

public interface GeneralFeatureIntensity<I extends ImageSingleBand,D extends ImageSingleBand> {

	/**
	 * Computes the corner's intensity.  Before computing the various image derivatives call
	 * {@link #getRequiresGradient()} and {@link #getRequiresHessian()} to see if they are needed.
	 *
	 * @param image Original input image
	 * @param derivX First derivative x-axis
	 * @param derivY First derivative x-axis
	 * @param derivXX Second derivative x-axis x-axis
	 * @param derivYY Second derivative x-axis y-axis
	 * @param derivXY Second derivative x-axis y-axis
	 *
	 */
	public void process( I image , D derivX , D derivY , D derivXX , D derivYY , D derivXY );

	/**
	 * Returns an image containing an intensity mapping showing how corner like each pixel is.
	 * Unprocessed image borders will have a value of zero.
	 *
	 * @return Corner intenisty image.
	 */
	public ImageFloat32 getIntensity();

	/**
	 * Optional: Returns a list of candidate locations for corners.  All other pixels are assumed to not be corners.
	 *
	 * @return List of potential corners.
	 */
	public QueueCorner getCandidates();

	/**
	 * If the image gradient is required for calculations.
	 *
	 * @return true if the image gradient is required.
	 */
	public boolean getRequiresGradient();

	/**
	 * Is the image's second derivative required?
	 *
	 * @return is the hessian required.
	 */
	public boolean getRequiresHessian();

	/**
	 * If true a list of candidate corners is returned.
	 */
	public boolean hasCandidates();

	/**
	 * Pixels within this distance from the image border are not processed.
	 *
	 * @return Size of unprocessed border around the image.
	 */
	public int getIgnoreBorder();
}
