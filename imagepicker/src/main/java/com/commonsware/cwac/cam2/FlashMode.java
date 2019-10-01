/***
 Copyright (c) 2015 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;

public enum FlashMode {
  OFF(Camera.Parameters.FLASH_MODE_OFF,
    CameraCharacteristics.CONTROL_AE_MODE_ON),
  TORCH(Camera.Parameters.FLASH_MODE_TORCH,
    CameraCharacteristics.CONTROL_AE_MODE_ON),
  ALWAYS(Camera.Parameters.FLASH_MODE_ON,
    CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH),
  AUTO(Camera.Parameters.FLASH_MODE_AUTO,
    CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH),
  REDEYE(Camera.Parameters.FLASH_MODE_RED_EYE,
    CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);

  private final String classicMode;
  private final int cameraTwoMode;

  static FlashMode lookupClassicMode(String classicMode) {
    if (classicMode.equals(OFF.getClassicMode())) {
      return(OFF);
    }
    else if (classicMode.equals(ALWAYS.getClassicMode())) {
      return(ALWAYS);
    }
    else if (classicMode.equals(AUTO.getClassicMode())) {
      return(AUTO);
    }
    else if (classicMode.equals(REDEYE.getClassicMode())) {
      return(REDEYE);
    }

    return(null);
  }

  static FlashMode lookupCameraTwoMode(int cameraTwoMode) {
    if (cameraTwoMode==OFF.getCameraTwoMode()) {
      return(OFF);
    }
    else if (cameraTwoMode==ALWAYS.getCameraTwoMode()) {
      return(ALWAYS);
    }
    else if (cameraTwoMode==AUTO.getCameraTwoMode()) {
      return(AUTO);
    }
    else if (cameraTwoMode==REDEYE.getCameraTwoMode()) {
      return(REDEYE);
    }

    return(null);
  }

  FlashMode(String classicMode, int cameraTwoMode) {
    this.classicMode=classicMode;
    this.cameraTwoMode=cameraTwoMode;
  }

  public String getClassicMode() {
    return(classicMode);
  }

  public int getCameraTwoMode() {
    return(cameraTwoMode);
  }

  public boolean isTorchMode() {
    return(Camera.Parameters.FLASH_MODE_TORCH.equals(classicMode));
  }
}
