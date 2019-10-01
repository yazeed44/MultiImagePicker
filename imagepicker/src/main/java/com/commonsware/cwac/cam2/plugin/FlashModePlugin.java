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

package com.commonsware.cwac.cam2.plugin;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.util.Log;
import android.view.OrientationEventListener;
import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.CameraConfigurator;
import com.commonsware.cwac.cam2.CameraPlugin;
import com.commonsware.cwac.cam2.CameraSession;
import com.commonsware.cwac.cam2.ClassicCameraConfigurator;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.SimpleCameraTwoConfigurator;
import com.commonsware.cwac.cam2.SimpleClassicCameraConfigurator;
import java.util.List;

/**
 * Plugin for managing flash modes
 */
public class FlashModePlugin implements CameraPlugin {
  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends CameraConfigurator> T buildConfigurator(Class<T> type) {
    if (type == ClassicCameraConfigurator.class) {
      return (type.cast(new Classic()));
    }

    return(type.cast(new Two()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(CameraSession session) {
    // no validation required
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    // no-op
  }

  class Classic extends SimpleClassicCameraConfigurator {
    /**
     * {@inheritDoc}
     */
    @Override
    public Camera.Parameters configureStillCamera(
      CameraSession session,
      Camera.CameraInfo info,
      Camera camera, Camera.Parameters params) {
      if (params!=null && session.getCurrentFlashMode()!=null) {
        params.setFlashMode(session.getCurrentFlashMode().getClassicMode());
      }

      return(params);
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  class Two extends SimpleCameraTwoConfigurator {
    /**
     * {@inheritDoc}
     */
    @Override
    public void addToCaptureRequest(CameraSession session,
                                    CameraCharacteristics cc,
                                    boolean facingFront,
                                    CaptureRequest.Builder captureBuilder) {
      if (session.getCurrentFlashMode()!=null) {
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
          session.getCurrentFlashMode().getCameraTwoMode());
        captureBuilder.set(CaptureRequest.FLASH_MODE,
          session.getCurrentFlashMode().isTorchMode() ?
            CameraMetadata.FLASH_MODE_TORCH :
            CameraMetadata.FLASH_MODE_OFF);
      }
    }

    @Override
    public void addToPreviewRequest(CameraSession session,
                                    CameraCharacteristics cc,
                                    CaptureRequest.Builder captureBuilder) {
      if (session.getCurrentFlashMode()!=null) {
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
          session.getCurrentFlashMode().getCameraTwoMode());
        captureBuilder.set(CaptureRequest.FLASH_MODE,
          session.getCurrentFlashMode().isTorchMode() ?
            CameraMetadata.FLASH_MODE_TORCH :
            CameraMetadata.FLASH_MODE_OFF);
      }
    }
  }
}
