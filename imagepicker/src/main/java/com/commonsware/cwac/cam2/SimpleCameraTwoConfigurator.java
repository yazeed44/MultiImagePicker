package com.commonsware.cwac.cam2;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;

/**
 * Stub implementation of CameraTwoConfigurator, to help ease
 * the implementation of this interface. Just extend this class
 * and override the specific methods that your plugin needs.
 */
public class SimpleCameraTwoConfigurator
  implements CameraTwoConfigurator {
  @Override
  public ImageReader buildImageReader() {
    return null;
  }

  @Override
  public void addToCaptureRequest(CameraSession session,
                                  CameraCharacteristics cc,
                                  boolean facingFront,
                                  CaptureRequest.Builder captureBuilder) {

  }

  @Override
  public void addToPreviewRequest(CameraSession session,
                                  CameraCharacteristics cc,
                                  CaptureRequest.Builder captureBuilder) {

  }
}
