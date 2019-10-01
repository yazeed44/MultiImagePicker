/**
 * Copyright (c) 2015 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.hardware.Camera;
import android.media.MediaRecorder;

/**
 * Camera configurator for the ClassicCameraEngine, to be used as
 * part of a CameraPlugin for configuring camera behavior.
 */
public interface ClassicCameraConfigurator extends CameraConfigurator {
  /**
   * Update the Camera.Parameters based on whatever this particular
   * configurator is set up to do.
   *
   * @param info CameraInfo for the camera that we are configuring
   * @param camera the camera we are configuring
   * @param params the current camera parameters, as originally
   *               retrieved from the camera and as modified by
   *               prior configurators in the chain
   * @return the same Camera.Parameters as was passed in as params,
   * just with modified contents
   */
  Camera.Parameters configureStillCamera(CameraSession session,
                                         Camera.CameraInfo info,
                                         Camera camera,
                                         Camera.Parameters params);

  void configureRecorder(CameraSession session,
                         int cameraId,
                         VideoTransaction xact,
                         MediaRecorder recorder);
}
