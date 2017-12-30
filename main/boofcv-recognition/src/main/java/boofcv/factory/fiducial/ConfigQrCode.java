/*
 * Copyright (c) 2011-2017, Peter Abeles. All Rights Reserved.
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

package boofcv.factory.fiducial;

import boofcv.abst.shapes.polyline.ConfigPolylineSplitMerge;
import boofcv.factory.filter.binary.ConfigThreshold;
import boofcv.factory.filter.binary.ConfigThresholdLocalOtsu;
import boofcv.factory.filter.binary.ThresholdType;
import boofcv.factory.shape.ConfigPolygonDetector;
import boofcv.struct.ConfigLength;
import boofcv.struct.Configuration;
import boofcv.struct.ConnectRule;

/**
 * TODO Comment
 *
 * @author Peter Abeles
 */
public class ConfigQrCode implements Configuration {

	public ConfigThreshold threshold;

	public ConfigPolygonDetector polygon = new ConfigPolygonDetector();

	public int versionMinimum = 1;
	public int versionMaximum = 40;

	{

		// 40% slower but better at detecting markers by a few percentage points
//		ConfigThreshold configThreshold = ConfigThreshold.local(ThresholdType.LOCAL_MEAN,10);
//		configThreshold.scale = 0.95;

		// fast but does a bad job detecting markers that are up close
		ConfigThresholdLocalOtsu configThreshold = ConfigThreshold.local(ThresholdType.BLOCK_OTSU,20);
		configThreshold.scale = 1.0;
		configThreshold.thresholdFromLocalBlocks = false;
		configThreshold.tuning = 5;

		threshold = configThreshold;

		polygon.detector.contourRule = ConnectRule.EIGHT;
		polygon.detector.clockwise = false;
		((ConfigPolylineSplitMerge)polygon.detector.contourToPoly).maxSideError = ConfigLength.relative(0.12,3);
		((ConfigPolylineSplitMerge)polygon.detector.contourToPoly).cornerScorePenalty = 0.4;
		((ConfigPolylineSplitMerge)polygon.detector.contourToPoly).minimumSideLength = 2;
		// 28 pixels = 7 by 7 square viewed head on. Each cell is then 1 pixel. Any slight skew results in
		// aliasing and will most likely not be read well.
		polygon.detector.minimumContour = ConfigLength.fixed(27);
		polygon.detector.minimumEdgeIntensity = 10;
		polygon.minimumRefineEdgeIntensity = 20;
	}

	@Override
	public void checkValidity() {
		// this is now manually set by the detector. previous settings don't matter
//		if( polygon.detector.clockwise )
//			throw new IllegalArgumentException("Must be counter clockwise");
//		if( polygon.detector.minimumSides != 4 || polygon.detector.maximumSides != 4)
//			throw new IllegalArgumentException("Must detect 4 sides and only 4 sides");

	}
}
