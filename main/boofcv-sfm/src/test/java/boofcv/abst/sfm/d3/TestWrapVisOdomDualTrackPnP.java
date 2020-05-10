/*
 * Copyright (c) 2011-2020, Peter Abeles. All Rights Reserved.
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

package boofcv.abst.sfm.d3;

import boofcv.factory.sfm.ConfigStereoDualTrackPnP;
import boofcv.factory.sfm.FactoryVisualOdometry;
import boofcv.struct.image.GrayF32;
import boofcv.struct.pyramid.ConfigDiscreteLevels;

/**
 * @author Peter Abeles
 */
public class TestWrapVisOdomDualTrackPnP extends CheckVisualOdometryStereoSim<GrayF32> {

	public TestWrapVisOdomDualTrackPnP() {super(GrayF32.class);}

	@Override
	public StereoVisualOdometry<GrayF32> createAlgorithm() {
		var config = new ConfigStereoDualTrackPnP();

		config.scene.bundleConverge.maxIterations = 10;
		config.scene.ransac.inlierThreshold = 1.5;
		config.tracker.klt.pyramidLevels = ConfigDiscreteLevels.levels(4);
		config.tracker.klt.templateRadius = 3;
		config.tracker.detDesc.detectPoint.shiTomasi.radius = 3;
		config.tracker.detDesc.detectPoint.general.radius = 3;

//		config.stereoRadius = 5;

		return FactoryVisualOdometry.stereoDualTrackerPnP(config,GrayF32.class);
	}
}