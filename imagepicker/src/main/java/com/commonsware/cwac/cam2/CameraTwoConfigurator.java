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

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;

/**
 * Camera configurator for the CameraTwoEngine, to be used as
 * part of a CameraPlugin for configuring camera behavior.
 */
public interface CameraTwoConfigurator extends CameraConfigurator {
  /**
   * @return an ImageReader to be used by the CameraTwoEngine,
   * or null if this configurator is not responsible for creating
   * the ImageReader
   */
  ImageReader buildImageReader();

  /**
   * Updates a CaptureRequest to reflect what the plugin needs.
   *
   * @param cc CameraCharacteristics for the camera being used
   * @param facingFront true if the camera is front-facing, false
   *                    otherwise
   * @param captureBuilder the builder for the request, to be
   *                       configured
   */
  void addToCaptureRequest(CameraSession session,
                           CameraCharacteristics cc,
                           boolean facingFront,
                           CaptureRequest.Builder captureBuilder);

  /**
   * Updates a preview CaptureRequest to reflect what the plugin needs.
   *
   * @param cc CameraCharacteristics for the camera being used
   * @param captureBuilder the builder for the request, to be
   *                       configured
   */
  void addToPreviewRequest(CameraSession session,
                           CameraCharacteristics cc,
                           CaptureRequest.Builder captureBuilder);
}
