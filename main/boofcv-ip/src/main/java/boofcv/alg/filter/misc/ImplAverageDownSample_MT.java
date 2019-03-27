/*
 * Copyright (c) 2011-2019, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.filter.misc;

import boofcv.concurrency.BoofConcurrency;
import boofcv.struct.image.*;

import javax.annotation.Generated;

/**
 * <p> * Overlays a rectangular grid on top of the src image and computes the average value within each cell
 * which is then written into the dst image. The dst image must be smaller than or equal to the src image.</p>
 *
 * <p>
 * <p>
 * DO NOT MODIFY. This code was automatically generated by GenerateImplAverageDownSample.
 * <p>
 * </p>
 *
 * @author Peter Abeles
 */
@Generated("boofcv.alg.filter.misc.GenerateImplAverageDownSample")
public class ImplAverageDownSample_MT {
	/**
	 * Down samples the image along the x-axis only.  Image height's must be the same.
	 * @param src Input image.  Not modified.
	 * @param dst Output image.  Modified.
	 */
	public static void horizontal( GrayU8 src , GrayF32 dst ) {

		if( src.width < dst.width )
			throw new IllegalArgumentException("src width must be >= dst width");
		if( src.height != dst.height )
			throw new IllegalArgumentException("src height must equal dst height");

		float scale = src.width/(float)dst.width;

		BoofConcurrency.loopFor(0, dst.height, y -> {
			int indexDst = dst.startIndex + y*dst.stride;
			for (int x = 0; x < dst.width-1; x++, indexDst++ ) {
				float srcX0 = x*scale;
				float srcX1 = (x+1)*scale;

				int isrcX0 = (int)srcX0;
				int isrcX1 = (int)srcX1;

				int index = src.getIndex(isrcX0,y);

				// compute value of overlapped region
				float startWeight = (1.0f-(srcX0-isrcX0));
				int start = src.data[index++]& 0xFF;

				int middle = 0;
				for( int i = isrcX0+1; i < isrcX1; i++ ) {
					middle += src.data[index++]& 0xFF;
				}

				float endWeight = (srcX1%1);
				int end = src.data[index]& 0xFF;
				dst.data[indexDst] = (start*startWeight + middle + end*endWeight)/scale;
			}

			// handle the last area as a special case
			int x = dst.width-1;
			float srcX0 = x*scale;

			int isrcX0 = (int)srcX0;
			int isrcX1 = src.width-1;

			int index = src.getIndex(isrcX0,y);

			// compute value of overlapped region
			float startWeight = (1.0f-(srcX0-isrcX0));
			int start = src.data[index++]& 0xFF;

			int middle = 0;
			for( int i = isrcX0+1; i < isrcX1; i++ ) {
				middle += src.data[index++]& 0xFF;
			}

			int end = isrcX1 != isrcX0 ? src.data[index]& 0xFF : 0;
			dst.data[indexDst] = (start*startWeight + middle + end)/scale;
		});
	}

	/**
	 * Down samples the image along the y-axis only.  Image width's must be the same.
	 * @param src Input image.  Not modified.
	 * @param dst Output image.  Modified.
	 */
	public static void vertical( GrayF32 src , GrayI8 dst ) {

		if( src.height < dst.height )
			throw new IllegalArgumentException("src height must be >= dst height");
		if( src.width != dst.width )
			throw new IllegalArgumentException("src width must equal dst width");

		float scale = src.height/(float)dst.height;

		BoofConcurrency.loopFor(0, dst.width, x -> {
			int indexDst = dst.startIndex + x;
			for (int y = 0; y < dst.height-1; y++) {

				float srcY0 = y*scale;
				float srcY1 = (y+1)*scale;

				int isrcY0 = (int)srcY0;
				int isrcY1 = (int)srcY1;

				int index = src.getIndex(x,isrcY0);

				// compute value of overlapped region
				float startWeight = (1.0f-(srcY0-isrcY0));
				float start = src.data[index];
				index += src.stride;

				float middle = 0;
				for( int i = isrcY0+1; i < isrcY1; i++ ) {
					middle += src.data[index];
					index += src.stride;
				}

				float endWeight = (srcY1%1);
				float end = src.data[index];
				dst.data[indexDst] = (byte)((start*startWeight + middle + end*endWeight)/scale + 0.5f);
				indexDst += dst.stride;
			}

			// handle the last area as a special case
			int y = dst.height-1;
			float srcY0 = y*scale;

			int isrcY0 = (int)srcY0;
			int isrcY1 = src.height-1;

			int index = src.getIndex(x,isrcY0);

			// compute value of overlapped region
			float startWeight = (1.0f-(srcY0-isrcY0));
			float start = src.data[index];
			index += src.stride;

			float middle = 0;
			for( int i = isrcY0+1; i < isrcY1; i++ ) {
				middle += src.data[index];
				index += src.stride;
			}

			float end = isrcY1 != isrcY0 ? src.data[index]: 0;
			dst.data[indexDst] = (byte)((start*startWeight + middle + end)/scale + 0.5f);
		});
	}

	/**
	 * Down samples the image along the x-axis only.  Image height's must be the same.
	 * @param src Input image.  Not modified.
	 * @param dst Output image.  Modified.
	 */
	public static void horizontal( GrayU16 src , GrayF32 dst ) {

		if( src.width < dst.width )
			throw new IllegalArgumentException("src width must be >= dst width");
		if( src.height != dst.height )
			throw new IllegalArgumentException("src height must equal dst height");

		float scale = src.width/(float)dst.width;

		BoofConcurrency.loopFor(0, dst.height, y -> {
			int indexDst = dst.startIndex + y*dst.stride;
			for (int x = 0; x < dst.width-1; x++, indexDst++ ) {
				float srcX0 = x*scale;
				float srcX1 = (x+1)*scale;

				int isrcX0 = (int)srcX0;
				int isrcX1 = (int)srcX1;

				int index = src.getIndex(isrcX0,y);

				// compute value of overlapped region
				float startWeight = (1.0f-(srcX0-isrcX0));
				int start = src.data[index++]& 0xFFFF;

				int middle = 0;
				for( int i = isrcX0+1; i < isrcX1; i++ ) {
					middle += src.data[index++]& 0xFFFF;
				}

				float endWeight = (srcX1%1);
				int end = src.data[index]& 0xFFFF;
				dst.data[indexDst] = (start*startWeight + middle + end*endWeight)/scale;
			}

			// handle the last area as a special case
			int x = dst.width-1;
			float srcX0 = x*scale;

			int isrcX0 = (int)srcX0;
			int isrcX1 = src.width-1;

			int index = src.getIndex(isrcX0,y);

			// compute value of overlapped region
			float startWeight = (1.0f-(srcX0-isrcX0));
			int start = src.data[index++]& 0xFFFF;

			int middle = 0;
			for( int i = isrcX0+1; i < isrcX1; i++ ) {
				middle += src.data[index++]& 0xFFFF;
			}

			int end = isrcX1 != isrcX0 ? src.data[index]& 0xFFFF : 0;
			dst.data[indexDst] = (start*startWeight + middle + end)/scale;
		});
	}

	/**
	 * Down samples the image along the y-axis only.  Image width's must be the same.
	 * @param src Input image.  Not modified.
	 * @param dst Output image.  Modified.
	 */
	public static void vertical( GrayF32 src , GrayI16 dst ) {

		if( src.height < dst.height )
			throw new IllegalArgumentException("src height must be >= dst height");
		if( src.width != dst.width )
			throw new IllegalArgumentException("src width must equal dst width");

		float scale = src.height/(float)dst.height;

		BoofConcurrency.loopFor(0, dst.width, x -> {
			int indexDst = dst.startIndex + x;
			for (int y = 0; y < dst.height-1; y++) {

				float srcY0 = y*scale;
				float srcY1 = (y+1)*scale;

				int isrcY0 = (int)srcY0;
				int isrcY1 = (int)srcY1;

				int index = src.getIndex(x,isrcY0);

				// compute value of overlapped region
				float startWeight = (1.0f-(srcY0-isrcY0));
				float start = src.data[index];
				index += src.stride;

				float middle = 0;
				for( int i = isrcY0+1; i < isrcY1; i++ ) {
					middle += src.data[index];
					index += src.stride;
				}

				float endWeight = (srcY1%1);
				float end = src.data[index];
				dst.data[indexDst] = (short)((start*startWeight + middle + end*endWeight)/scale + 0.5f);
				indexDst += dst.stride;
			}

			// handle the last area as a special case
			int y = dst.height-1;
			float srcY0 = y*scale;

			int isrcY0 = (int)srcY0;
			int isrcY1 = src.height-1;

			int index = src.getIndex(x,isrcY0);

			// compute value of overlapped region
			float startWeight = (1.0f-(srcY0-isrcY0));
			float start = src.data[index];
			index += src.stride;

			float middle = 0;
			for( int i = isrcY0+1; i < isrcY1; i++ ) {
				middle += src.data[index];
				index += src.stride;
			}

			float end = isrcY1 != isrcY0 ? src.data[index]: 0;
			dst.data[indexDst] = (short)((start*startWeight + middle + end)/scale + 0.5f);
		});
	}

	/**
	 * Down samples the image along the x-axis only.  Image height's must be the same.
	 * @param src Input image.  Not modified.
	 * @param dst Output image.  Modified.
	 */
	public static void horizontal( GrayF32 src , GrayF32 dst ) {

		if( src.width < dst.width )
			throw new IllegalArgumentException("src width must be >= dst width");
		if( src.height != dst.height )
			throw new IllegalArgumentException("src height must equal dst height");

		float scale = src.width/(float)dst.width;

		BoofConcurrency.loopFor(0, dst.height, y -> {
			int indexDst = dst.startIndex + y*dst.stride;
			for (int x = 0; x < dst.width-1; x++, indexDst++ ) {
				float srcX0 = x*scale;
				float srcX1 = (x+1)*scale;

				int isrcX0 = (int)srcX0;
				int isrcX1 = (int)srcX1;

				int index = src.getIndex(isrcX0,y);

				// compute value of overlapped region
				float startWeight = (1.0f-(srcX0-isrcX0));
				float start = src.data[index++];

				float middle = 0;
				for( int i = isrcX0+1; i < isrcX1; i++ ) {
					middle += src.data[index++];
				}

				float endWeight = (srcX1%1);
				float end = src.data[index];
				dst.data[indexDst] = (start*startWeight + middle + end*endWeight)/scale;
			}

			// handle the last area as a special case
			int x = dst.width-1;
			float srcX0 = x*scale;

			int isrcX0 = (int)srcX0;
			int isrcX1 = src.width-1;

			int index = src.getIndex(isrcX0,y);

			// compute value of overlapped region
			float startWeight = (1.0f-(srcX0-isrcX0));
			float start = src.data[index++];

			float middle = 0;
			for( int i = isrcX0+1; i < isrcX1; i++ ) {
				middle += src.data[index++];
			}

			float end = isrcX1 != isrcX0 ? src.data[index] : 0;
			dst.data[indexDst] = (start*startWeight + middle + end)/scale;
		});
	}

	/**
	 * Down samples the image along the y-axis only.  Image width's must be the same.
	 * @param src Input image.  Not modified.
	 * @param dst Output image.  Modified.
	 */
	public static void vertical( GrayF32 src , GrayF32 dst ) {

		if( src.height < dst.height )
			throw new IllegalArgumentException("src height must be >= dst height");
		if( src.width != dst.width )
			throw new IllegalArgumentException("src width must equal dst width");

		float scale = src.height/(float)dst.height;

		BoofConcurrency.loopFor(0, dst.width, x -> {
			int indexDst = dst.startIndex + x;
			for (int y = 0; y < dst.height-1; y++) {

				float srcY0 = y*scale;
				float srcY1 = (y+1)*scale;

				int isrcY0 = (int)srcY0;
				int isrcY1 = (int)srcY1;

				int index = src.getIndex(x,isrcY0);

				// compute value of overlapped region
				float startWeight = (1.0f-(srcY0-isrcY0));
				float start = src.data[index];
				index += src.stride;

				float middle = 0;
				for( int i = isrcY0+1; i < isrcY1; i++ ) {
					middle += src.data[index];
					index += src.stride;
				}

				float endWeight = (srcY1%1);
				float end = src.data[index];
				dst.data[indexDst] = (float)((start*startWeight + middle + end*endWeight)/scale );
				indexDst += dst.stride;
			}

			// handle the last area as a special case
			int y = dst.height-1;
			float srcY0 = y*scale;

			int isrcY0 = (int)srcY0;
			int isrcY1 = src.height-1;

			int index = src.getIndex(x,isrcY0);

			// compute value of overlapped region
			float startWeight = (1.0f-(srcY0-isrcY0));
			float start = src.data[index];
			index += src.stride;

			float middle = 0;
			for( int i = isrcY0+1; i < isrcY1; i++ ) {
				middle += src.data[index];
				index += src.stride;
			}

			float end = isrcY1 != isrcY0 ? src.data[index]: 0;
			dst.data[indexDst] = (float)((start*startWeight + middle + end)/scale );
		});
	}

	/**
	 * Down samples the image along the x-axis only.  Image height's must be the same.
	 * @param src Input image.  Not modified.
	 * @param dst Output image.  Modified.
	 */
	public static void horizontal( GrayF64 src , GrayF64 dst ) {

		if( src.width < dst.width )
			throw new IllegalArgumentException("src width must be >= dst width");
		if( src.height != dst.height )
			throw new IllegalArgumentException("src height must equal dst height");

		float scale = src.width/(float)dst.width;

		BoofConcurrency.loopFor(0, dst.height, y -> {
			int indexDst = dst.startIndex + y*dst.stride;
			for (int x = 0; x < dst.width-1; x++, indexDst++ ) {
				float srcX0 = x*scale;
				float srcX1 = (x+1)*scale;

				int isrcX0 = (int)srcX0;
				int isrcX1 = (int)srcX1;

				int index = src.getIndex(isrcX0,y);

				// compute value of overlapped region
				float startWeight = (1.0f-(srcX0-isrcX0));
				double start = src.data[index++];

				double middle = 0;
				for( int i = isrcX0+1; i < isrcX1; i++ ) {
					middle += src.data[index++];
				}

				float endWeight = (srcX1%1);
				double end = src.data[index];
				dst.data[indexDst] = (start*startWeight + middle + end*endWeight)/scale;
			}

			// handle the last area as a special case
			int x = dst.width-1;
			float srcX0 = x*scale;

			int isrcX0 = (int)srcX0;
			int isrcX1 = src.width-1;

			int index = src.getIndex(isrcX0,y);

			// compute value of overlapped region
			float startWeight = (1.0f-(srcX0-isrcX0));
			double start = src.data[index++];

			double middle = 0;
			for( int i = isrcX0+1; i < isrcX1; i++ ) {
				middle += src.data[index++];
			}

			double end = isrcX1 != isrcX0 ? src.data[index] : 0;
			dst.data[indexDst] = (start*startWeight + middle + end)/scale;
		});
	}

	/**
	 * Down samples the image along the y-axis only.  Image width's must be the same.
	 * @param src Input image.  Not modified.
	 * @param dst Output image.  Modified.
	 */
	public static void vertical( GrayF64 src , GrayF64 dst ) {

		if( src.height < dst.height )
			throw new IllegalArgumentException("src height must be >= dst height");
		if( src.width != dst.width )
			throw new IllegalArgumentException("src width must equal dst width");

		float scale = src.height/(float)dst.height;

		BoofConcurrency.loopFor(0, dst.width, x -> {
			int indexDst = dst.startIndex + x;
			for (int y = 0; y < dst.height-1; y++) {

				float srcY0 = y*scale;
				float srcY1 = (y+1)*scale;

				int isrcY0 = (int)srcY0;
				int isrcY1 = (int)srcY1;

				int index = src.getIndex(x,isrcY0);

				// compute value of overlapped region
				float startWeight = (1.0f-(srcY0-isrcY0));
				double start = src.data[index];
				index += src.stride;

				double middle = 0;
				for( int i = isrcY0+1; i < isrcY1; i++ ) {
					middle += src.data[index];
					index += src.stride;
				}

				float endWeight = (srcY1%1);
				double end = src.data[index];
				dst.data[indexDst] = (double)((start*startWeight + middle + end*endWeight)/scale );
				indexDst += dst.stride;
			}

			// handle the last area as a special case
			int y = dst.height-1;
			float srcY0 = y*scale;

			int isrcY0 = (int)srcY0;
			int isrcY1 = src.height-1;

			int index = src.getIndex(x,isrcY0);

			// compute value of overlapped region
			float startWeight = (1.0f-(srcY0-isrcY0));
			double start = src.data[index];
			index += src.stride;

			double middle = 0;
			for( int i = isrcY0+1; i < isrcY1; i++ ) {
				middle += src.data[index];
				index += src.stride;
			}

			double end = isrcY1 != isrcY0 ? src.data[index]: 0;
			dst.data[indexDst] = (double)((start*startWeight + middle + end)/scale );
		});
	}

}
