/***
 Copyright (c) 2015-2016 CommonsWare, LLC

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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Activity for recording video. Analogue of CameraActivity.
 * Supports the same protocol, in terms of extras and return data,\
 * as does ACTION_VIDEO_CAPTURE.
 */
public class VideoRecorderActivity extends AbstractCameraActivity {
  /**
   * Whether there should be a timer shown on the video recording
   * preview screen, and, if so, whether it counts down the remaining
   * time on a time-limited recording or whether it counts up for how
   * long the current recording is. Value should be a ChronoType
   * enum value (NONE, COUNT_DOWN, COUNT_UP). COUNT_DOWN will be
   * ignored unless you also provide a duration limit.
   */
  public static final String EXTRA_CHRONOTYPE=
    "cwac_cam2_chronotype";

  private static final String[] PERMS={
    Manifest.permission.CAMERA,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.RECORD_AUDIO};

  @Override
  protected String[] getNeededPermissions() {
    return(PERMS);
  }

  @Override
  protected boolean needsOverlay() {
    return(false);
  }

  @Override
  protected boolean needsActionBar() {
    return(false);
  }

  @Override
  protected boolean isVideo() {
    return(true);
  }

  @Override
  protected void configEngine(CameraEngine engine) {
    // no-op
  }

  @Override
  protected CameraFragment buildFragment() {
    return(CameraFragment.newVideoInstance(getOutputUri(),
        getIntent().getBooleanExtra(EXTRA_UPDATE_MEDIA_STORE, false),
        getIntent().getIntExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1),
        getIntent().getIntExtra(MediaStore.EXTRA_SIZE_LIMIT, 0),
        getIntent().getIntExtra(MediaStore.EXTRA_DURATION_LIMIT, 0),
        (ZoomStyle)getIntent().getSerializableExtra(EXTRA_ZOOM_STYLE),
        getIntent().getBooleanExtra(EXTRA_FACING_EXACT_MATCH, false),
        (ChronoType)getIntent().getSerializableExtra(EXTRA_CHRONOTYPE),
        getIntent().getBooleanExtra(EXTRA_SHOW_RULE_OF_THIRDS_GRID, false)));
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventMainThread(CameraEngine.VideoTakenEvent event) {
    if (event.getVideoTransaction()==null) {
      setResult(RESULT_CANCELED);
      finish();
      // TODO: something with the exception
    }
    else {
      findViewById(android.R.id.content).post(() -> {
       setResult(RESULT_OK, buildResultIntent().setData(getOutputUri()));
       finish();
      });
    }
  }

  /**
   * Class to build an Intent used to start the VideoRecorderActivity.
   * Call setComponent() on the Intent if you are using your
   * own subclass of VideoRecorderActivity.
   */
  public static class IntentBuilder
    extends AbstractCameraActivity.IntentBuilder<IntentBuilder> {
    /**
     * Standard constructor. May throw a runtime exception
     * if the environment is not set up properly (see
     * validateEnvironment() on Utils).
     *
     * @param ctxt any Context will do
     */
    public IntentBuilder(Context ctxt) {
      super(ctxt, VideoRecorderActivity.class);
    }

    @Override
    public Intent buildChooserBaseIntent() {
      return(new Intent(MediaStore.ACTION_VIDEO_CAPTURE));
    }

    @Override
    public Intent build() {
      forceEngine(CameraEngine.ID.CLASSIC);

      return(super.build());
    }

    @Override
    public IntentBuilder to(Uri output) {
      if (!"file".equals(output.getScheme())) {
        throw new IllegalArgumentException("must be a file:/// Uri");
      }

      return(super.to(output));
    }

    /**
     * Sets the maximum size of the video file in bytes. Maps
     * to EXTRA_SIZE_LIMIT.
     *
     * @param limit maximum video size in bytes
     * @return the builder, for chaining
     */
    public IntentBuilder sizeLimit(int limit) {
      result.putExtra(MediaStore.EXTRA_SIZE_LIMIT, limit);

      return(this);
    }

    /**
     * Sets the maximum duration of the video file in milliseconds. Maps
     * to EXTRA_DURATION_LIMIT.
     *
     * @param limit maximum video length in milliseconds
     * @return the builder, for chaining
     */
    public IntentBuilder durationLimit(int limit) {
      result.putExtra(MediaStore.EXTRA_DURATION_LIMIT, limit);

      return(this);
    }

    /**
     * Whether there should be a timer shown on the video recording
     * preview screen, and, if so, whether it counts down the remaining
     * time on a time-limited recording or whether it counts up for how
     * long the current recording is. COUNT_DOWN will be
     * ignored unless you also provide a duration limit.
     *
     * @param type NONE, COUNT_DOWN, or COUNT_UP
     * @return the builder, for chaining
     */
    public IntentBuilder chronoType(ChronoType type) {
      result.putExtra(EXTRA_CHRONOTYPE, type);

      return(this);
    }
  }
}
