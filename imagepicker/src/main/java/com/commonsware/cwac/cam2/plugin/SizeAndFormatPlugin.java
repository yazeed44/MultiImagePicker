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
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import com.commonsware.cwac.cam2.CameraConfigurator;
import com.commonsware.cwac.cam2.CameraConstraints;
import com.commonsware.cwac.cam2.CameraPlugin;
import com.commonsware.cwac.cam2.CameraSession;
import com.commonsware.cwac.cam2.CameraTwoConfigurator;
import com.commonsware.cwac.cam2.ClassicCameraConfigurator;
import com.commonsware.cwac.cam2.SimpleCameraTwoConfigurator;
import com.commonsware.cwac.cam2.SimpleClassicCameraConfigurator;
import com.commonsware.cwac.cam2.VideoTransaction;
import com.commonsware.cwac.cam2.util.Size;

/**
 * A plugin that configures the size and format of previews and
 * pictures to be taken by the camera. This, or a plugin like it,
 * needs to be in the plugin chain for the CameraSession.
 */
public class SizeAndFormatPlugin implements CameraPlugin {
  final private Size pictureSize;
  final private Size previewSize;
  private final int pictureFormat;

  /**
   * Constructor.
   *
   * @param previewSize the size of preview images
   * @param pictureSize the size of pictures to be taken
   * @param pictureFormat the format of pictures to be taken, in
   *                      the form of an ImageFormat constant
   *                      (e.g., ImageFormat.JPEG)
   */
  public SizeAndFormatPlugin(Size previewSize, Size pictureSize, int pictureFormat) {
    this.previewSize=previewSize;
    this.pictureSize=pictureSize;
    this.pictureFormat=pictureFormat;
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
    if (!session.getDescriptor().getPreviewSizes().contains(previewSize)) {
      throw new IllegalStateException("Requested preview size is not one that the camera supports");
    }

    if (!session.getDescriptor().getPictureSizes().contains(pictureSize)) {
      throw new IllegalStateException("Requested picture size is not one that the camera supports");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    // not required
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
        params.setPreviewSize(previewSize.getWidth(),
          previewSize.getHeight());
        params.setPictureSize(pictureSize.getWidth(),
          pictureSize.getHeight());
        params.setPictureFormat(pictureFormat);
      }

      return(params);
    }

    @Override
    public void configureRecorder(CameraSession session,
                                  int cameraId,
                                  VideoTransaction xact,
                                  MediaRecorder recorder) {
      int highProfile=getHigh();

      boolean canGoHigh=CamcorderProfile.hasProfile(cameraId,
        highProfile);
      boolean canGoLow=CamcorderProfile.hasProfile(cameraId,
        CamcorderProfile.QUALITY_LOW);

      if (canGoHigh && (xact.getQuality()==1 || !canGoLow)) {
        recorder.setProfile(CamcorderProfile.get(cameraId,
          highProfile));
      }
      else if (canGoLow) {
        recorder.setProfile(CamcorderProfile.get(cameraId,
          CamcorderProfile.QUALITY_LOW));
      }
      else {
        throw new IllegalStateException(
          "cannot find valid CamcorderProfile");
      }
    }

    private int getHigh() {
      CameraConstraints constraints=CameraConstraints.get();

      if (constraints!=null) {
        return(constraints.getHighCamcorderProfile());
      }

      return(CamcorderProfile.QUALITY_HIGH);
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  class Two extends SimpleCameraTwoConfigurator {
    /**
     * {@inheritDoc}
     */
    @Override
    public ImageReader buildImageReader() {
      return(ImageReader.newInstance(pictureSize.getWidth(),
          pictureSize.getHeight(), pictureFormat, 2));
    }
  }
}
