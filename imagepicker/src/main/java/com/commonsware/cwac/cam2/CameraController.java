/***
 * Copyright (c) 2015 CommonsWare, LLC
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may
 * not use this file except in compliance with the License. You may
 * obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the License for the specific language governing permissions
 * and
 * limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.View;
import com.commonsware.cwac.cam2.plugin.FlashModePlugin;
import com.commonsware.cwac.cam2.plugin.FocusModePlugin;
import com.commonsware.cwac.cam2.plugin.OrientationPlugin;
import com.commonsware.cwac.cam2.plugin.SizeAndFormatPlugin;
import com.commonsware.cwac.cam2.util.Size;
import com.commonsware.cwac.cam2.util.Utils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

/**
 * Controller for camera-related functions, designed to be used
 * by CameraFragment or the equivalent.
 */
public class CameraController implements CameraView.StateCallback {
  private final boolean allowChangeFlashMode;
  private CameraEngine engine;
  private CameraSession session;
  private List<CameraDescriptor> cameras=null;
  private int currentCamera=0;
  private final HashMap<CameraDescriptor, CameraView> previews=
    new HashMap<CameraDescriptor, CameraView>();
  private Queue<CameraView> availablePreviews=null;
  private boolean switchPending=false;
  private boolean isVideoRecording=false;
  private final FocusMode focusMode;
  private final boolean isVideo;
  private FlashModePlugin flashModePlugin;
  private int zoomLevel=0;
  private int quality=0;
  private final ResultReceiver onError;

  public CameraController(FocusMode focusMode,
                          ResultReceiver onError,
                          boolean allowChangeFlashMode,
                          boolean isVideo) {
    this.onError=onError;
    this.focusMode=focusMode==null ?
      FocusMode.CONTINUOUS : focusMode;
    this.isVideo=isVideo;
    this.allowChangeFlashMode=allowChangeFlashMode;
  }

  /**
   * @return the engine being used by this fragment to access
   * the camera(s) on the device
   */
  public CameraEngine getEngine() {
    return (engine);
  }

  /**
   * Setter for the engine. Must be called before onCreateView()
   * is called, preferably shortly after constructing the
   * fragment.
   *
   * @param engine the engine to be used by this fragment to access
   * the camera(s) on the device
   */
  public void setEngine(CameraEngine engine,
                        CameraSelectionCriteria criteria) {
    this.engine=engine;

    AbstractCameraActivity.BUS.register(this);

    engine.loadCameraDescriptors(criteria);
  }

  public int getNumberOfCameras() {
    return (cameras==null ? 0 : cameras.size());
  }

  /**
   * Call this from onStart() of an activity or fragment, or from
   * an equivalent point in time. If the CameraView is ready,
   * the preview should begin; otherwise, the preview will
   * begin after the CameraView is ready.
   */
  public void start() {
    if (cameras!=null) {
      CameraDescriptor camera=cameras.get(currentCamera);
      CameraView cv=getPreview(camera);

      if (cv.isAvailable()) {
        open();
      }
    }
  }

  /**
   * Call this from onStop() of an activity or fragment, or
   * from an equivalent point in time, to indicate that you want
   * the camera preview to stop.
   */
  public void stop() throws Exception {
    if (session!=null) {
      stopVideoRecording(true);

      CameraSession temp=session;

      session=null;
      engine.close(temp);
      // session.destroy(); -- moved into engines
    }
  }

  /**
   * Call this from onDestroy() of an activity or fragment,
   * or from an equivalent point in time, to tear down the
   * entire controller. A fresh controller should
   * be created if you want to use the camera again in the future.
   */
  public void destroy() {
    AbstractCameraActivity.BUS.post(
      new ControllerDestroyedEvent(this));
    AbstractCameraActivity.BUS.unregister(this);
  }

  /**
   * Call to switch to the next camera in sequence. Most
   * devices have only two cameras, and so calling this will
   * switch the preview and pictures to the camera other than
   * the one presently being used.
   */
  public void switchCamera() throws Exception {
    if (session!=null) {
      getPreview(session.getDescriptor()).setVisibility(
        View.INVISIBLE);
      switchPending=true;
      stop();
    }
  }

  /**
   * Supplies CameraView objects for each camera. After this,
   * we can open() the camera.
   *
   * @param cameraViews a list of CameraViews
   */
  public void setCameraViews(Queue<CameraView> cameraViews) {
    availablePreviews=cameraViews;
    previews.clear();

    for (CameraView cv : cameraViews) {
      cv.setStateCallback(this);
    }

    open(); // in case visible CameraView is already ready
  }

  /**
   * Public because Java interfaces are intrinsically public.
   * This method is not part of the class' API and should not
   * be used by third-party developers.
   *
   * @param cv the CameraView that is now ready
   */
  @Override
  public void onReady(CameraView cv) {
    if (cameras!=null) {
      open();
    }
  }

  /**
   * Public because Java interfaces are intrinsically public.
   * This method is not part of the class' API and should not
   * be used by third-party developers.
   *
   * @param cv the CameraView that is now destroyed
   */
  @Override
  public void onDestroyed(CameraView cv) throws Exception {
    stop();
  }

  /**
   * Takes a picture, in accordance with the details supplied
   * in the PictureTransaction. Subscribe to the
   * PictureTakenEvent to get the results of the picture.
   *
   * @param xact a PictureTransaction describing what should be taken
   */
  public void takePicture(PictureTransaction xact) {
    if (session!=null) {
      AbstractCameraActivity.BUS.post(new PictureCaptureStartEvent());
      engine.takePicture(session, xact);
    }
  }

  public void recordVideo(VideoTransaction xact) throws Exception {
    if (session!=null) {
      engine.recordVideo(session, xact);
      isVideoRecording=true;
    }
  }

  public void stopVideoRecording(boolean abandon) throws Exception {
    if (session!=null && isVideoRecording) {
      try {
        engine.stopVideoRecording(session, abandon);
      }
      finally {
        isVideoRecording=false;
      }
    }
  }

  public void setQuality(int quality) {
    this.quality=quality;
  }

  public boolean canToggleFlashMode() {
    return (allowChangeFlashMode &&
      engine.supportsDynamicFlashModes() &&
      engine.hasMoreThanOneEligibleFlashMode());
  }

  public FlashMode getCurrentFlashMode() {
    return (session.getCurrentFlashMode());
  }

  public boolean supportsZoom() {
    return (engine.supportsZoom(session));
  }

  public int getCurrentCamera() {
    return (currentCamera);
  }

  public void setCurrentCamera(int currentCamera) {
    this.currentCamera=currentCamera;
  }

  public boolean changeZoom(int delta) {
    zoomLevel+=delta;

    return (handleZoom());
  }

  public boolean setZoom(int zoomLevel) {
    this.zoomLevel=zoomLevel;

    return (handleZoom());
  }

  private boolean handleZoom() {
    if (zoomLevel<0) {
      zoomLevel=0;
    }
    else if (zoomLevel>100) {
      zoomLevel=100;
    }

    return (engine.zoomTo(session, zoomLevel));
  }

  private CameraView getPreview(CameraDescriptor camera) {
    CameraView result=previews.get(camera);

    if (result==null && availablePreviews!=null) {
      result=availablePreviews.remove();
      previews.put(camera, result);
    }

    return (result);
  }

  private int getNextCameraIndex() {
    int next=currentCamera+1;

    if (next==cameras.size()) {
      next=0;
    }

    return (next);
  }

  private void open() {
    if (session==null) {
      Size previewSize=null;
      CameraDescriptor camera=cameras.get(currentCamera);
      CameraView cv=getPreview(camera);
      Size pictureSize;

      if (quality>0) {
        pictureSize=Utils.getLargestPictureSize(camera);
      }
      else {
        pictureSize=Utils.getSmallestPictureSize(camera);
      }

      if (camera!=null && cv.getWidth()>0 && cv.getHeight()>0) {
        previewSize=Utils.chooseOptimalSize(camera.getPreviewSizes(),
          cv.getWidth(), cv.getHeight(), pictureSize);
      }

      SurfaceTexture texture=cv.getSurfaceTexture();

      if (previewSize!=null && texture!=null) {
        if (Build.VERSION.SDK_INT>=
          Build.VERSION_CODES.JELLY_BEAN_MR1) {
          texture.setDefaultBufferSize(previewSize.getWidth(),
            previewSize.getHeight());
        }

        flashModePlugin=new FlashModePlugin();

        session=engine
          .buildSession(cv.getContext(), camera)
          .addPlugin(new SizeAndFormatPlugin(previewSize,
            pictureSize, ImageFormat.JPEG))
          .addPlugin(new OrientationPlugin(cv.getContext()))
          .addPlugin(
            new FocusModePlugin(cv.getContext(), focusMode, isVideo))
          .addPlugin(flashModePlugin)
          .build();

        session.setPreviewSize(previewSize);
        engine.open(session, texture);
      }
    }
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode=ThreadMode.MAIN)
  public void onEventMainThread(
    CameraEngine.CameraDescriptorsEvent event) {
    if (event.exception!=null) {
      postError(ErrorConstants.ERROR_LIST_CAMERAS, event.exception);
    }

    if (event.descriptors.size()>0) {
      cameras=event.descriptors;
      AbstractCameraActivity.BUS.post(
        new ControllerReadyEvent(this, cameras.size()));
    }
    else {
      AbstractCameraActivity.BUS.post(new NoSuchCameraEvent());
    }
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode=ThreadMode.MAIN)
  public void onEventMainThread(CameraEngine.OpenedEvent event) {
    if (event.exception!=null) {
      // handled at fragment level
    }
    else {
      CameraDescriptor camera=cameras.get(currentCamera);
      CameraView cv=getPreview(camera);

      if (cv!=null) {
        boolean shouldSwapPreviewDimensions=
          cv
            .getContext()
            .getResources()
            .getConfiguration().orientation==
            Configuration.ORIENTATION_PORTRAIT;
        Size virtualPreviewSize=session.getPreviewSize();
        /*
         * Size will always be like this: 4032x3024, 2688x1512 or 800x600, so in portrait mode,
         * it is mandatory to swap these values or preview will stretch
         */
        if (shouldSwapPreviewDimensions) {
          virtualPreviewSize=
            new Size(session.getPreviewSize().getHeight(),
              session.getPreviewSize().getWidth());
        }

        cv.setPreviewSize(virtualPreviewSize);
      }
    }
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode=ThreadMode.MAIN)
  public void onEventMainThread(CameraEngine.ClosedEvent event) {
    if (event.exception!=null) {
      postError(ErrorConstants.ERROR_CLOSE_CAMERA, event.exception);
      AbstractCameraActivity.BUS.post(new NoSuchCameraEvent());
    }
    else {
      if (switchPending) {
        switchPending=false;
        currentCamera=getNextCameraIndex();
        getPreview(cameras.get(currentCamera))
          .setVisibility(View.VISIBLE);
        open();
      }
    }
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode=ThreadMode.MAIN)
  public void onEventMainThread(
    CameraEngine.OrientationChangedEvent event) {
    if (engine!=null) {
      engine.handleOrientationChange(session, event);
    }
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode=ThreadMode.MAIN)
  public void onEventMainThread(CameraEngine.DeepImpactEvent event) {
    postError(ErrorConstants.ERROR_MISC, event.exception);
  }

  public void postError(int resultCode, Throwable e) {
    if (onError!=null) {
      Bundle resultData=new Bundle();
      StringWriter sw=new StringWriter();

      e.printStackTrace(new PrintWriter(sw));
      resultData
        .putString(ErrorConstants.RESULT_STACK_TRACE,
          sw.toString());
      onError.send(resultCode, resultData);
    }
  }

  /**
   * Raised if there are no available cameras on this
   * device. Consider using uses-feature elements in the
   * manifest, so your app only runs on devices that have
   * a camera, if you need a camera.
   */
  public static class NoSuchCameraEvent {

  }

  /**
   * Event raised when the controller has its cameras
   * and is ready for use. Clients should then turn
   * around and call setCameraViews() to complete the process
   * and start showing the first preview.
   */
  public static class ControllerReadyEvent {
    final private int cameraCount;
    final private CameraController ctlr;

    private ControllerReadyEvent(CameraController ctlr,
                                 int cameraCount) {
      this.cameraCount=cameraCount;
      this.ctlr=ctlr;
    }

    public int getNumberOfCameras() {
      return (cameraCount);
    }

    public boolean isEventForController(CameraController ctlr) {
      return (this.ctlr==ctlr);
    }
  }

  /**
   * Event raised when the controller has its cameras
   * and is ready for use. Clients should then turn
   * around and call setCameraViews() to complete the process
   * and start showing the first preview.
   */
  public static class ControllerDestroyedEvent {
    private final CameraController ctlr;

    ControllerDestroyedEvent(CameraController ctlr) {
      this.ctlr=ctlr;
    }

    public CameraController getDestroyedController() {
      return (ctlr);
    }
  }

  public static class PictureCaptureStartEvent {

  }
}
