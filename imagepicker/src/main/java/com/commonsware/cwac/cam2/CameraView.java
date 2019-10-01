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

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import com.commonsware.cwac.cam2.util.Size;

/**
 * Class responsible for rendering preview frames on the UI
 * as a View for the user to see and interact with. Also handles
 * maintaining aspect ratios and dealing with full-bleed previews.
 */
public class CameraView extends TextureView implements TextureView.SurfaceTextureListener {
  public interface StateCallback {
    void onReady(CameraView cv);
    void onDestroyed(CameraView cv) throws Exception;
  }

  /**
   * The requested size of the preview frames, or null to just
   * use the size of the view itself
   */
  private Size previewSize;
  private StateCallback stateCallback;
  private boolean mirror=false;

  /**
   * Constructor, used for creating instances from Java code.
   *
   * @param context the Activity that will host this View
   */
  public CameraView(Context context) {
    super(context, null);
    initListener();
  }

  /**
   * Constructor, used by layout inflation.
   *
   * @param context the Activity that will host this View
   * @param attrs the parsed attributes from the layout resource tag
   */
  public CameraView(Context context, AttributeSet attrs) {
    super(context, attrs, 0);
    initListener();
  }

  /**
   * Constructor, used by... something.
   *
   * @param context the Activity that will host this View
   * @param attrs the parsed attributes from the layout resource tag
   * @param defStyle "An attribute in the current theme that
   *                 contains a reference to a style resource
   *                 that supplies default values for the view.
   *                 Can be 0 to not look for defaults."
   */
  public CameraView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initListener();
  }

  /**
   * @return the requested preview size
   */
  public Size getPreviewSize() {
    return(previewSize);
  }

  /**
   * @param previewSize the requested preview size
   */
  public void setPreviewSize(Size previewSize) {
    this.previewSize=previewSize;

    enterTheMatrix();
    requestLayout();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int width=MeasureSpec.getSize(widthMeasureSpec);
    int height=MeasureSpec.getSize(heightMeasureSpec);
    boolean isFullBleed=true;

    if (previewSize==null) {
      setMeasuredDimension(width, height);
    }
    else {
      if (isFullBleed) {
        if (width>height*previewSize.getWidth()/previewSize.getHeight()) {
          setMeasuredDimension(width,
            width*previewSize.getHeight()/previewSize.getWidth());
        }
        else {
          setMeasuredDimension(height*previewSize.getWidth()/previewSize.getHeight(),
            height);
        }
      }
      else {
        if (width<height*previewSize.getWidth()/previewSize.getHeight()) {
          setMeasuredDimension(width,
            width*previewSize.getHeight()/previewSize.getWidth());
        }
        else {
          setMeasuredDimension(height*previewSize.getWidth()/previewSize.getHeight(),
            height);
        }
      }
    }
  }

  public void setStateCallback(StateCallback cb) {
    stateCallback=cb;
  }

  public void setMirror(boolean mirror) {
    this.mirror=mirror;
  }

  private void initListener() {
    setSurfaceTextureListener(this);
  }

  @Override
  public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
    if (stateCallback!=null) {
      stateCallback.onReady(this);
    }
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
    enterTheMatrix();
  }

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
    if (stateCallback!=null) {
      try {
        stateCallback.onDestroyed(this);
      }
      catch (Exception e) {
        Log.e(getClass().getSimpleName(),
          "Exception destroying state", e);
      }
    }

    return(false);
  }

  @Override
  public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

  }

  private void enterTheMatrix() {
    if (previewSize!=null) {
      adjustAspectRatio(previewSize.getWidth(),
          previewSize.getHeight(),
          ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRotation());
    }
  }

  // based on https://github.com/googlesamples/android-Camera2Basic/blob/master/Application/src/main/java/com/example/android/camera2basic/Camera2BasicFragment.java

  private void adjustAspectRatio(int previewWidth,
                                 int previewHeight,
                                 int rotation) {
    Matrix txform=new Matrix();
    int viewWidth=getWidth();
    int viewHeight=getHeight();
    RectF rectView=new RectF(0, 0, viewWidth, viewHeight);
    float viewCenterX=rectView.centerX();
    float viewCenterY=rectView.centerY();
    RectF rectPreview=new RectF(0, 0, previewHeight, previewWidth);
    float previewCenterX=rectPreview.centerX();
    float previewCenterY=rectPreview.centerY();

    if (Surface.ROTATION_90==rotation ||
      Surface.ROTATION_270==rotation) {
      rectPreview.offset(viewCenterX-previewCenterX,
        viewCenterY-previewCenterY);

      txform.setRectToRect(rectView, rectPreview,
        Matrix.ScaleToFit.FILL);

      float scale=Math.max((float)viewHeight/previewHeight,
        (float)viewWidth/previewWidth);

      txform.postScale(scale, scale, viewCenterX, viewCenterY);
      txform.postRotate(90*(rotation-2), viewCenterX,
        viewCenterY);
    }
    else {
      if (Surface.ROTATION_180==rotation) {
        txform.postRotate(180, viewCenterX, viewCenterY);
      }
    }

    if (mirror) {
      txform.postScale(-1, 1, viewCenterX, viewCenterY);
    }

    setTransform(txform);
  }
}
