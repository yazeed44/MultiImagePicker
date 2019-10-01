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
import com.commonsware.cwac.cam2.CameraConfigurator;
import com.commonsware.cwac.cam2.CameraConstraints;
import com.commonsware.cwac.cam2.CameraPlugin;
import com.commonsware.cwac.cam2.CameraSession;
import com.commonsware.cwac.cam2.ClassicCameraConfigurator;
import com.commonsware.cwac.cam2.FocusMode;
import com.commonsware.cwac.cam2.SimpleCameraTwoConfigurator;
import com.commonsware.cwac.cam2.SimpleClassicCameraConfigurator;

/**
 * Plugin for managing focus modes
 */
public class FocusModePlugin implements CameraPlugin {
  private final Context ctxt;
  private final boolean isVideo;
  private final FocusMode focusMode;
  private OrientationEventListener orientationEventListener;
  private int lastOrientation=OrientationEventListener.ORIENTATION_UNKNOWN;

  public FocusModePlugin(Context ctxt,
                         FocusMode focusMode,
                         boolean isVideo) {
    this.ctxt=ctxt.getApplicationContext();
    this.focusMode=focusMode;
    this.isVideo=isVideo;

    orientationEventListener=new OrientationEventListener(ctxt) {
      @Override
      public void onOrientationChanged(int orientation) {
        lastOrientation=orientation;
      }
    };

    if (orientationEventListener.canDetectOrientation()) {
      orientationEventListener.enable();
    }
    else {
      orientationEventListener.disable();
      orientationEventListener=null;
    }
  }

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
    if (orientationEventListener!=null) {
      orientationEventListener.disable();
      orientationEventListener=null;
    }
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
      if (params!=null) {
        String desiredMode=null;

        if (focusMode==FocusMode.OFF) {
          desiredMode=Camera.Parameters.FOCUS_MODE_FIXED;
        }
        else if (focusMode==
          FocusMode.EDOF) {
          desiredMode=Camera.Parameters.FOCUS_MODE_EDOF;
        }
        else if (focusMode==
          FocusMode.MACRO) {
          desiredMode=Camera.Parameters.FOCUS_MODE_MACRO;
        }
        else if (isVideo) {
          desiredMode=
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
        }
        else {
          desiredMode=
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        }

        if (params.getSupportedFocusModes().contains(desiredMode)) {
          params.setFocusMode(desiredMode);
        }
        else {
          Log.e("CWAC-Cam2",
            "no support for requested focus mode");
        }
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
    public void addToPreviewRequest(CameraSession session,
                                    CameraCharacteristics cc,
                                    CaptureRequest.Builder captureBuilder) {
      int desiredMode=getDesiredFocusMode(cc);

      if (desiredMode!=-1) {
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
          desiredMode);
      }
      else {
        Log.e("CWAC-Cam2", "no support for requested focus mode");
      }
    }

    @Override
    public void addToCaptureRequest(CameraSession session,
                                    CameraCharacteristics cc,
                                    boolean facingFront,
                                    CaptureRequest.Builder captureBuilder) {
      int desiredMode=getDesiredFocusMode(cc);

      if (desiredMode!=-1) {
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
          desiredMode);
      }
      else {
        Log.e("CWAC-Cam2", "no support for requested focus mode");
      }
    }

    private int getDesiredFocusMode(CameraCharacteristics cc) {
      int[] availModes=cc.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
      int desiredMode;

      if (focusMode==FocusMode.OFF) {
        desiredMode=CameraMetadata.CONTROL_AF_MODE_OFF;
      }
      else if (focusMode==FocusMode.EDOF) {
        desiredMode=CameraMetadata.CONTROL_AF_MODE_EDOF;
      }
      else if (focusMode==FocusMode.MACRO) {
        desiredMode=CameraMetadata.CONTROL_AF_MODE_MACRO;
      }
      else if (isVideo) {
        desiredMode=CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
      }
      else {
        desiredMode=CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
      }

      CameraConstraints constraints=CameraConstraints.get();

      if (constraints!=null && constraints.getDisableFocusMode()) {
        desiredMode=CameraMetadata.CONTROL_AF_MODE_OFF;
      }

      for (int availMode : availModes) {
        if (availMode==desiredMode) {
          return(desiredMode);
        }
      }

      return(-1);
    }
  }
}
