package com.commonsware.cwac.cam2;

import android.hardware.Camera;
import android.media.MediaRecorder;

/**
 * Stub no-op implementation of ClassicCameraConfigurator,
 * to simplify implementing the interface. Just extend this class,
 * then override the particular methods that you need in the plugin.
 */
public class SimpleClassicCameraConfigurator implements ClassicCameraConfigurator {
  @Override
  public Camera.Parameters configureStillCamera(
    CameraSession session,
    Camera.CameraInfo info,
    Camera camera, Camera.Parameters params) {
    return null;
  }

  @Override
  public void configureRecorder(CameraSession session,
                                int cameraId,
                                VideoTransaction xact,
                                MediaRecorder recorder) {
    // no-op
  }
}
