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

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import com.commonsware.cwac.cam2.util.Size;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of a CameraEngine that supports the
 * original android.hardware.Camera API.
 */
@SuppressWarnings("deprecation")
public class ClassicCameraEngine extends CameraEngine
    implements MediaRecorder.OnInfoListener,
    Camera.PreviewCallback, Camera.OnZoomChangeListener {
  private final Context ctxt;
  private List<Descriptor> descriptors=null;
  private MediaRecorder recorder;
  private VideoTransaction xact;
  private int previewWidth, previewHeight;
  private int previewFormat;

  public ClassicCameraEngine(Context ctxt) {
    this.ctxt=ctxt.getApplicationContext();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CameraSession.Builder buildSession(Context ctxt, CameraDescriptor descriptor) {
    return(new SessionBuilder(ctxt, descriptor));
  }

  /**
   * {@inheritDoc}
   */
  public void loadCameraDescriptors(final CameraSelectionCriteria criteria) {
    getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (descriptors == null) {
            int count=Camera.getNumberOfCameras();
            List<Descriptor> result=new ArrayList<Descriptor>();
            Camera.CameraInfo info=new Camera.CameraInfo();

            for (int cameraId=0; cameraId<count; cameraId++) {
              Camera.getCameraInfo(cameraId, info);
              CameraConstraints constraint=CameraConstraints.get();

              if (constraint!=null) {
                if (!constraint.supportsFFC() && info.facing==
                  Camera.CameraInfo.CAMERA_FACING_FRONT) {
                  break;
                }

                if (!constraint.supportsRFC() && info.facing==
                  Camera.CameraInfo.CAMERA_FACING_BACK) {
                  break;
                }
              }

              Descriptor descriptor=new Descriptor(cameraId, info);

              Camera camera=Camera.open(descriptor.getCameraId());
              Camera.Parameters params=camera.getParameters();

              if (params!=null) {
                List<Size> sizes=null;

                if (constraint!=null) {
                  if (info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    sizes=constraint.getPreviewFFCSizeWhitelist();
                  }
                  else {
                    sizes=constraint.getPreviewRFCSizeWhitelist();
                  }
                }

                if (sizes==null) {
                  sizes=new ArrayList<>();

                  for (Camera.Size size : params.getSupportedPreviewSizes()) {
                    if (size.height<2160 && size.width<2160) {
                      sizes.add(new Size(size.width, size.height));
//  Log.e("20161211", String.format("%d %d x %d", info.facing, size.width, size.height));
                    }
                  }
                }

                descriptor.setPreviewSizes(sizes);
                sizes=null;

                if (constraint!=null) {
                  if (info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    sizes=constraint.getPictureFFCSizeWhitelist();
                  }
                  else {
                    sizes=constraint.getPictureRFCSizeWhitelist();
                  }
                }

                if (sizes==null) {
                  sizes=new ArrayList<>();

                  for (Camera.Size size : params.getSupportedPictureSizes()) {
                    if (!"samsung".equals(Build.MANUFACTURER) ||
                      !"jflteuc".equals(Build.PRODUCT) ||
                      size.width<2048) {
                      sizes.add(new Size(size.width, size.height));
                    }
                  }
                }

                descriptor.setPictureSizes(sizes);
                result.add(descriptor);
              }

              camera.release();
            }

            descriptors=result;
          }

          List<CameraDescriptor> result=
            new ArrayList<CameraDescriptor>();

          for (Descriptor descriptor : descriptors) {
            if ((!criteria.getFacingExactMatch() ||
              descriptor.getScore(criteria)>0) &&
              !criteria.isVideo() ||
              (descriptor.facing==Camera.CameraInfo.CAMERA_FACING_FRONT &&
              CameraConstraints.get().supportsFFCVideo()) ||
              (descriptor.facing!=Camera.CameraInfo.CAMERA_FACING_FRONT &&
                CameraConstraints.get().supportsRFCVideo())) {
              result.add(descriptor);
            }
          }

          Collections.sort(result,
                  (descriptor, t1) -> {
                    Descriptor lhs=(Descriptor)descriptor;
                    Descriptor rhs=(Descriptor)t1;

                    // descending, so invert normal side-ness

                    int lhScore=rhs.getScore(criteria);
                    int rhScore=lhs.getScore(criteria);

                    // from Integer.compare(), which is new to API Level 19

                    return (lhScore<rhScore ? -1 :
                      (lhScore==rhScore ? 0 : 1));
                  });

          getBus().post(
            new CameraEngine.CameraDescriptorsEvent(result));
        }
        catch (Exception e) {
          getBus().post(new DeepImpactEvent(e));

          if (isDebug()) {
            Log.w(getClass().getSimpleName(), "Exception opening camera", e);
          }
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close(final CameraSession session) {
    Descriptor descriptor=(Descriptor)session.getDescriptor();
    Camera camera=descriptor.getCamera();

    if (camera != null) {
      camera.stopPreview();
      camera.release();
      descriptor.setCamera(null);
    }

    session.destroy();
    getBus().post(new ClosedEvent());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void takePicture(final CameraSession session, final PictureTransaction xact) {
    getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        Descriptor descriptor=(Descriptor)session.getDescriptor();
        Camera camera=descriptor.getCamera();

        if (savePreviewFile()!=null) {
          camera.setOneShotPreviewCallback(ClassicCameraEngine.this);

          Camera.Parameters parameters=camera.getParameters();

          previewWidth=parameters.getPreviewSize().width;
          previewHeight=parameters.getPreviewSize().height;
          previewFormat=parameters.getPreviewFormat();
        }

        try {
          camera.takePicture(() -> {
            // empty plays a sound -- go figure
          }, null,
              new TakePictureTransaction(session.getContext(), xact));
        }
        catch (Exception e) {
          getBus().post(new PictureTakenEvent(e));

          if (isDebug()) {
            Log.e(getClass().getSimpleName(), "Exception taking picture", e);
          }
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void open(final CameraSession session,
                   final SurfaceTexture texture) {
    getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        Descriptor descriptor=(Descriptor)session.getDescriptor();
        Camera camera=descriptor.getCamera();

        if (camera == null) {
          camera=Camera.open(descriptor.getCameraId());
          descriptor.setCamera(camera);
        }

        Camera.Parameters params=camera.getParameters();
        List<String> rawFlashModes=params.getSupportedFlashModes();

        eligibleFlashModes.clear();

        if (rawFlashModes!=null && preferredFlashModes!=null) {
          for (FlashMode flashMode : preferredFlashModes) {
            for (String rawFlashMode : rawFlashModes) {
              if (rawFlashMode.equals(
                flashMode.getClassicMode())) {
                eligibleFlashModes.add(flashMode);
                break;
              }
            }
          }

          if (eligibleFlashModes.isEmpty()) {
            for (String rawFlashMode : rawFlashModes) {
              FlashMode flashMode=
                FlashMode.lookupClassicMode(rawFlashMode);

              if (flashMode!=null) {
                eligibleFlashModes.add(flashMode);
              }
            }
          }

          session.setCurrentFlashMode(eligibleFlashModes.get(0));
        }

        try {
          camera.setParameters(((Session)session).configureStillCamera(
            false));
          camera.setPreviewTexture(texture);
          camera.startPreview();
          getBus().post(new OpenedEvent());
        }
        catch (Exception e) {
          camera.release();
          descriptor.setCamera(null);
          getBus().post(new OpenedEvent(e));

          if (isDebug()) {
            Log.e(getClass().getSimpleName(), "Exception opening camera", e);
          }
        }
      }
    });
  }

  @Override
  public void recordVideo(CameraSession session,
                          VideoTransaction xact) throws Exception {
    Descriptor descriptor=(Descriptor)session.getDescriptor();
    Camera camera=descriptor.getCamera();

    if (camera!=null) {
      camera.stopPreview();
      camera.unlock();

      try {
        recorder=new MediaRecorder();
        recorder.setCamera(camera);
        recorder.setAudioSource(
          MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        ((Session)session).configureRecorder(xact, recorder);

        recorder.setOutputFile(xact.getOutputPath().getAbsolutePath());
        recorder.setMaxFileSize(xact.getSizeLimit());
        recorder.setMaxDuration(xact.getDurationLimit());
        recorder.setOnInfoListener(this);
        recorder.prepare();
        recorder.start();
        this.xact=xact;
      }
      catch (IOException e) {
        recorder.release();
        recorder=null;
        throw e;
      }
    }
  }

  @Override
  public void stopVideoRecording(CameraSession session,
                                 boolean abandon) throws Exception {
    Descriptor descriptor=(Descriptor)session.getDescriptor();
    Camera camera=descriptor.getCamera();

    if (camera!=null && recorder!=null) {
      MediaRecorder tempRecorder=recorder;

      recorder=null;

      tempRecorder.stop();
      tempRecorder.release();

      if (!abandon) {
        camera.reconnect();
        camera.startPreview();
      }
    }

    if (!abandon) {
      getBus().post(new VideoTakenEvent(xact));
    }

    xact=null;
  }

/*
  @Override
  public boolean shouldSwapPreviewDimensions(CameraSession session) {
    WindowManager windowManager=
      (WindowManager)ctxt.getSystemService(Context.WINDOW_SERVICE);
    Configuration config=ctxt.getResources().getConfiguration();
    int rotation=windowManager.getDefaultDisplay().getRotation();

    boolean defaultLandscapeAndIsInLandscape = (rotation == Surface.ROTATION_0 ||
      rotation == Surface.ROTATION_180) &&
      config.orientation == Configuration.ORIENTATION_LANDSCAPE;

    boolean defaultLandscapeAndIsInPortrait = (rotation == Surface.ROTATION_90 ||
      rotation == Surface.ROTATION_270) &&
      config.orientation == Configuration.ORIENTATION_PORTRAIT;

    return(defaultLandscapeAndIsInLandscape ||
      defaultLandscapeAndIsInPortrait);
  }
*/

  @Override
  public void handleOrientationChange(CameraSession session,
                                      OrientationChangedEvent event) {
    if (session!=null) {
      ((Session)session).configureStillCamera(true);
    }
  }

  @Override
  public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
    if (what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
        what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED ||
        what==MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
      MediaRecorder tempRecorder=recorder;

      recorder=null;

      if (tempRecorder != null) {
        tempRecorder.stop();
        tempRecorder.release();
      }

      getBus().post(new VideoTakenEvent(xact));
    }
  }

  @Override
  public void onPreviewFrame(final byte[] data, final Camera camera) {
    new Thread() {
      @Override
      public void run() {
        YuvImage yuv=new YuvImage(data, previewFormat,
          previewWidth, previewHeight, null);

        try {
          if (savePreviewFile().exists()) {
            savePreviewFile().delete();
          }

          FileOutputStream fos=
            new FileOutputStream(savePreviewFile());

          yuv.compressToJpeg(new Rect(0, 0, previewWidth, previewHeight),
            90, fos);
          fos.flush();
          fos.getFD().sync();
          fos.close();
        }
        catch (Exception e) {
          Log.e(getClass().getSimpleName(),
            "Exception saving preview frame", e);
        }
      }
    }.start();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supportsDynamicFlashModes() {
    return(false);
  }

  @Override
  public boolean supportsZoom(CameraSession session) {
    Descriptor descriptor=(Descriptor)session.getDescriptor();
    Camera camera=descriptor.getCamera();
    Camera.Parameters params=camera.getParameters();

    return(params.isZoomSupported());
  }

  @Override
  public boolean zoomTo(CameraSession session, int zoomLevel) {
    Descriptor descriptor=(Descriptor)session.getDescriptor();
    Camera camera=descriptor.getCamera();
    Camera.Parameters params=camera.getParameters();
    int zoom=zoomLevel*params.getMaxZoom()/100;
    boolean result=false;

    if (params.isSmoothZoomSupported()) {
      camera.setZoomChangeListener(this);
      camera.startSmoothZoom(zoom);
      result=true;
    }
    else if (params.isZoomSupported()) {
      params.setZoom(zoom);
      camera.setParameters(params);
    }

    return(result);
  }

  @Override
  public void onZoomChange(int zoomValue, boolean stopped,
                           Camera camera) {
    if (stopped) {
      AbstractCameraActivity.BUS.post(new SmoothZoomCompletedEvent());
    }
  }

  private class TakePictureTransaction implements Camera.PictureCallback {
    private final PictureTransaction xact;
    private final Context ctxt;

    TakePictureTransaction(Context ctxt, PictureTransaction xact) {
      this.ctxt=ctxt.getApplicationContext();
      this.xact=xact;
    }

    @Override
    public void onPictureTaken(final byte[] bytes, final Camera camera) {
      getThreadPool().execute(() -> {
        camera.startPreview();
        getBus().post(new PictureTakenEvent(xact,
          xact.process(new ImageContext(ctxt, bytes))));
      });
    }
  }

  static class Descriptor implements CameraDescriptor {
    private final int cameraId;
    private Camera camera;
    private List<Size> pictureSizes;
    private List<Size> previewSizes;
    private final int facing;

    private Descriptor(int cameraId, Camera.CameraInfo info) {
      this.cameraId=cameraId;
      this.facing=info.facing;
    }

    public int getCameraId() {
      return (cameraId);
    }

    private void setCamera(Camera camera) {
      this.camera=camera;
    }

    private Camera getCamera() {
      return (camera);
    }

    @Override
    public List<Size> getPreviewSizes() {
      return (previewSizes);
    }

    private void setPreviewSizes(List<Size> sizes) {
      previewSizes=sizes;
    }

    @Override
    public List<Size> getPictureSizes() {
      return (pictureSizes);
    }

    @Override
    public boolean isPictureFormatSupported(int format) {
      return (ImageFormat.JPEG == format);
    }

    private void setPictureSizes(List<Size> sizes) {
      pictureSizes=sizes;
    }

    private int getScore(CameraSelectionCriteria criteria) {
      int score=10;

      if (criteria != null) {
        if ((criteria.getFacing().isFront() &&
            facing != Camera.CameraInfo.CAMERA_FACING_FRONT) ||
            (!criteria.getFacing().isFront() &&
                facing != Camera.CameraInfo.CAMERA_FACING_BACK)) {
          score=0;
        }
      }

      return(score);
    }
  }

  private static class Session extends CameraSession {
    private Session(Context ctxt, CameraDescriptor descriptor) {
      super(ctxt, descriptor);
    }

    Camera.Parameters configureStillCamera(boolean noParams) {
      final Descriptor descriptor=(Descriptor)getDescriptor();
      final Camera camera=descriptor.getCamera();
      Camera.Parameters params=null;

      if (camera!=null) {
        Camera.CameraInfo info=new Camera.CameraInfo();

        if (!noParams) {
          params=camera.getParameters();
        }

        Camera.getCameraInfo(descriptor.getCameraId(), info);

        for (CameraPlugin plugin : getPlugins()) {
          ClassicCameraConfigurator configurator=
            plugin.buildConfigurator(
              ClassicCameraConfigurator.class);

          if (configurator!=null) {
            params=
              configurator.configureStillCamera(this, info, camera,
                params);
          }
        }
      }

      return(params);
    }

    void configureRecorder(VideoTransaction xact,
                           MediaRecorder recorder) {
      final Descriptor descriptor=(Descriptor)getDescriptor();

      for (CameraPlugin plugin : getPlugins()) {
        ClassicCameraConfigurator configurator=
          plugin.buildConfigurator(ClassicCameraConfigurator.class);

        if (configurator!=null) {
          configurator.configureRecorder(this, descriptor.getCameraId(),
            xact, recorder);
        }
      }
    }
  }

  private static class SessionBuilder extends CameraSession.Builder {
    private SessionBuilder(Context ctxt, CameraDescriptor descriptor) {
      super(new Session(ctxt, descriptor));
    }
  }
}