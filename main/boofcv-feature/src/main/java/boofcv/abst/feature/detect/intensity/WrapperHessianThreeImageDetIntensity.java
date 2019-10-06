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

package boofcv.abst.feature.detect.intensity;

import boofcv.alg.filter.derivative.HessianThreeDeterminant;
import boofcv.core.image.border.FactoryImageBorder;
import boofcv.struct.QueueCorner;
import boofcv.struct.border.BorderType;
import boofcv.struct.border.ImageBorder_F32;
import boofcv.struct.border.ImageBorder_S32;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageGray;

/**
 * Computes the Hessian determinant directory from the input image. Ignores other derivatives passed on
 *
 * @author Peter Abeles
 */
public class WrapperHessianThreeImageDetIntensity<I extends ImageGray<I>,D extends ImageGray<D>>
		extends BaseGeneralFeatureIntensity<I,D>
{
	ImageBorder_F32 borderF32 = (ImageBorder_F32)FactoryImageBorder.single(GrayF32.class,BorderType.EXTENDED);
	ImageBorder_S32 borderS32 = (ImageBorder_S32<GrayU8>)FactoryImageBorder.single(GrayU8.class,BorderType.EXTENDED);


	@Override
	public void process(I image, D derivX, D derivY, D derivXX, D derivYY, D derivXY) {
		init(image.width,image.height);
		if( image instanceof GrayU8) {
			HessianThreeDeterminant.process((GrayU8)image,intensity,borderS32);
		} else if( image instanceof GrayF32) {
			HessianThreeDeterminant.process((GrayF32)image,intensity, borderF32);
		} else {
			throw new IllegalArgumentException("Unsupported input image type");
		}
	}

	@Override
	public QueueCorner getCandidatesMin() {
		return null;
	}

	@Override
	public QueueCorner getCandidatesMax() {
		return null;
	}

	@Override
	public boolean getRequiresGradient() {
		return false;
	}

	@Override
	public boolean getRequiresHessian() {
		return false;
	}

	@Override
	public boolean hasCandidates() {
		return false;
	}

	@Override
	public int getIgnoreBorder() {
		return 0;
	}

	@Override
	public boolean localMinimums() {
		return true;
	}

	@Override
	public boolean localMaximums() {
		return true;
	}
}
