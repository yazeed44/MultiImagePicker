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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;

import com.commonsware.cwac.cam2.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

/**
 * Base class for activities that integrate with CameraFragment
 * for taking pictures or recording video.
 */
abstract public class AbstractCameraActivity extends Activity {
    /**
     * List<FlashMode> indicating the desired flash modes,
     * or null for always taking the default. These are
     * considered in priority-first order (i.e., we will use
     * the first FlashMode if the device supports it, otherwise
     * we will use the second FlashMode, ...). If there is no
     * match, whatever the default device behavior is will be
     * used.
     */
    public static final String EXTRA_FLASH_MODES =
            "cwac_cam2_flash_modes";

    /**
     * True if we should allow the user to change the flash mode
     * on the fly (if the camera supports it), false otherwise.
     * Defaults to false.
     */
    public static final String EXTRA_ALLOW_SWITCH_FLASH_MODE =
            "cwac_cam2_allow_switch_flash_mode";

    /**
     * A ResultReceiver to be invoked on any error that the library
     * cannot handle internally.
     */
    public static final String EXTRA_UNHANDLED_ERROR_RECEIVER =
            "cwac_cam2_unhandled_error_receiver";

    /**
     * Extra name for indicating what facing rule for the
     * camera you wish to use. The value should be a
     * CameraSelectionCriteria.Facing instance.
     */
    public static final String EXTRA_FACING = "cwac_cam2_facing";

    /**
     * Extra name for indicating that the requested facing
     * must be an exact match, without gracefully degrading to
     * whatever camera happens to be available. If set to true,
     * requests to take a picture, for which the desired camera
     * is not available, will be cancelled. Defaults to false.
     */
    public static final String EXTRA_FACING_EXACT_MATCH =
            "cwac_cam2_facing_exact_match";

    /**
     * Extra name for indicating whether extra diagnostic
     * information should be reported, particularly for errors.
     * Default is false.
     */
    public static final String EXTRA_DEBUG_ENABLED = "cwac_cam2_debug";

    /**
     * Extra name for indicating if MediaStore should be updated
     * to reflect a newly-taken picture. Only relevant if
     * a file:// Uri is used. Default to false.
     */
    public static final String EXTRA_UPDATE_MEDIA_STORE =
            "cwac_cam2_update_media_store";

    /**
     * DO NOT USE. Use EXTRA_FORCE_ENGINE instead, please.
     */
    @Deprecated
    public static final String EXTRA_FORCE_CLASSIC = "cwac_cam2_force_classic";

    /**
     * If set to a CameraEngine.ID value (CLASSIC or CAMERA2), will
     * force the use of that engine. If left null/unset, the default
     * is based on what device we are running on.
     */
    public static final String EXTRA_FORCE_ENGINE = "cwac_cam2_force_engine";

    /**
     * If set to true, horizontally flips or mirrors the preview.
     * Does not change the picture or video output. Used mostly for FFC,
     * though will be honored for any camera. Defaults to false.
     */
    public static final String EXTRA_MIRROR_PREVIEW = "cwac_cam2_mirror_preview";

    /**
     * Extra name for focus mode to apply. Value should be one of the
     * FocusMode enum values. Default is CONTINUOUS.
     * If the desired focus mode is not available, the device default
     * focus mode is used.
     */
    public static final String EXTRA_FOCUS_MODE = "cwac_cam2_focus_mode";

    /**
     * Extra name for orientation lock mode to apply. Value should be
     * one of the OrientationLockMode values. Default, shockingly,
     * is DEFAULT.
     */
    public static final String EXTRA_ORIENTATION_LOCK_MODE =
            "cwac_cam2_olock_mode";

    /**
     * Extra name for whether the camera should allow zoom and
     * how. Value should be a ZoomStyle (NONE, PINCH, SEEKBAR).
     * Default is NONE.
     */
    public static final String EXTRA_ZOOM_STYLE =
            "cwac_cam2_zoom_style";

    /**
     * Extra name for runtime permission policy. If true, we check
     * for runtime permissions and fail fast if they are not already
     * granted. If false, if we lack runtime permissions (and need them
     * based on API level), we request them ourselves. Defaults to true.
     */
    public static final String EXTRA_FAIL_IF_NO_PERMISSION =
            "cwac_cam2_fail_if_no_permission";

    /**
     * Extra name for whether the camera should show a "rule of thirds"
     * overlay above the camera preview. Defaults to false.
     */
    public static final String EXTRA_SHOW_RULE_OF_THIRDS_GRID =
            "cwac_cam2_show_rule_of_thirds_grid";

    /**
     * @return true if the activity wants FEATURE_ACTION_BAR_OVERLAY,
     * false otherwise
     */
    abstract protected boolean needsOverlay();

    /**
     * @return false if we should hide the action bar outright
     * (ignored if needsOverlay() returns true)
     */
    abstract protected boolean needsActionBar();

    /**
     * @return true if we are recording a video, false if we are
     * taking a still picture
     */
    abstract protected boolean isVideo();

    /**
     * @return a CameraFragment for the given circumstances
     */
    abstract protected CameraFragment buildFragment();

    /**
     * @return array of the names of the permissions needed by
     * this activity
     */
    abstract protected String[] getNeededPermissions();

    /**
     * Configure the CameraEngine for things that are specific
     * to a subclass.
     *
     * @param engine the CameraEngine to configure
     */
    abstract protected void configEngine(CameraEngine engine);

    protected static final String TAG_CAMERA = CameraFragment.class.getCanonicalName();
    private static final int REQUEST_PERMS = 13401;
    protected CameraFragment cameraFrag;
    public static final EventBus BUS = new EventBus();

    /**
     * Standard lifecycle method, serving as the main entry
     * point of the activity.
     *
     * @param savedInstanceState the state of a previous instance
     */
    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.validateEnvironment(this, failIfNoPermissions());

        OrientationLockMode olockMode =
                (OrientationLockMode) getIntent().getSerializableExtra(EXTRA_ORIENTATION_LOCK_MODE);

        lockOrientation(olockMode);

        if (needsOverlay()) {
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

            // the following is nasty stuff to get rid of the action
            // bar drop shadow, which still exists on some devices
            // despite going into overlay mode (Samsung Galaxy S3, I'm
            // looking at you)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActionBar ab = getActionBar();

                if (ab != null) {
                    getActionBar().setElevation(0);
                }
            } else {
                View v = ((ViewGroup) getWindow().getDecorView()).getChildAt(0);

                if (v != null) {
                    v.setWillNotDraw(true);
                }
            }

        } else if (!needsActionBar()) {
            ActionBar ab = getActionBar();

            if (ab != null) {
                ab.hide();
            }
        }

        if (useRuntimePermissions()) {
            String[] perms = netPermissions(getNeededPermissions());

            if (perms.length == 0) {
                init();
            } else if (!failIfNoPermissions()) {
                requestPermissions(perms, REQUEST_PERMS);
            } else {
                throw new IllegalStateException("We lack the necessary permissions!");
            }
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String[] perms = netPermissions(getNeededPermissions());

        if (perms.length == 0) {
            init();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Standard lifecycle method, for when the fragment moves into
     * the started state. Passed along to the CameraController.
     */
    @Override
    public void onStart() {
        super.onStart();

        BUS.register(this);
    }

    /**
     * Standard lifecycle method, for when the fragment moves into
     * the stopped state. Passed along to the CameraController.
     */
    @Override
    public void onStop() {
        BUS.unregister(this);

        if (cameraFrag != null) {
            if (isChangingConfigurations()) {
                cameraFrag.stopVideoRecording();
            } else {
                cameraFrag.shutdown();
            }
        }

        super.onStop();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            cameraFrag.performCameraAction();

            return (true);
        }

        return (super.onKeyUp(keyCode, event));
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CameraController.NoSuchCameraEvent event) {
        finish();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CameraController.ControllerDestroyedEvent event) {
        finish();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CameraEngine.CameraTwoGenericEvent event) {
        finish();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CameraEngine.DeepImpactEvent event) {
        finish();
    }

    protected Uri getOutputUri() {
        Uri output = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ClipData clipData = getIntent().getClipData();

            if (clipData != null && clipData.getItemCount() > 0) {
                output = clipData.getItemAt(0).getUri();
            }
        }

        if (output == null) {
            output = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        }

        return (output);
    }

    protected void init() {
        cameraFrag = (CameraFragment) getFragmentManager().findFragmentByTag(TAG_CAMERA);

        boolean fragNeedsToBeAdded = false;

        if (cameraFrag == null) {
            cameraFrag = buildFragment();
            fragNeedsToBeAdded = true;
        }

        FocusMode focusMode =
                (FocusMode) getIntent().getSerializableExtra(EXTRA_FOCUS_MODE);
        boolean allowChangeFlashMode =
                getIntent().getBooleanExtra(EXTRA_ALLOW_SWITCH_FLASH_MODE, false);
        ResultReceiver onError =
                getIntent().getParcelableExtra(EXTRA_UNHANDLED_ERROR_RECEIVER);

        CameraController ctrl =
                new CameraController(focusMode, onError,
                        allowChangeFlashMode, isVideo());

        cameraFrag.setController(ctrl);
        cameraFrag
                .setMirrorPreview(getIntent()
                        .getBooleanExtra(EXTRA_MIRROR_PREVIEW, false));

        Facing facing =
                (Facing) getIntent().getSerializableExtra(EXTRA_FACING);

        if (facing == null) {
            facing = Facing.BACK;
        }

        boolean match = getIntent()
                .getBooleanExtra(EXTRA_FACING_EXACT_MATCH, false);
        CameraSelectionCriteria criteria =
                new CameraSelectionCriteria.Builder()
                        .facing(facing)
                        .facingExactMatch(match)
                        .isVideo(isVideo())
                        .build();
        CameraEngine.ID forcedEngineId =
                (CameraEngine.ID) getIntent().getSerializableExtra(EXTRA_FORCE_ENGINE);

        ctrl.setEngine(CameraEngine.buildInstance(this, forcedEngineId), criteria);
        ctrl.getEngine().setDebug(getIntent().getBooleanExtra(EXTRA_DEBUG_ENABLED, false));
        configEngine(ctrl.getEngine());

        if (fragNeedsToBeAdded) {
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, cameraFrag, TAG_CAMERA)
                    .commit();
        }
    }

    boolean canSwitchSources() {
        return (!getIntent().getBooleanExtra(EXTRA_FACING_EXACT_MATCH, false));
    }

    protected void lockOrientation(OrientationLockMode mode) {
        if (mode == null || mode == OrientationLockMode.DEFAULT) {
            int orientation = getResources().getConfiguration().orientation;

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(SCREEN_ORIENTATION_UNSPECIFIED);
            }
        } else if (mode == OrientationLockMode.LANDSCAPE) {
            setRequestedOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(
                    SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    protected Intent buildResultIntent() {
        return (new Intent());
    }

    @TargetApi(23)
    private boolean hasPermission(String perm) {
        if (useRuntimePermissions()) {
            return (checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED);
        }

        return (true);
    }

    private boolean useRuntimePermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private boolean failIfNoPermissions() {
        return (getIntent().getBooleanExtra(EXTRA_FAIL_IF_NO_PERMISSION, true));
    }

    private String[] netPermissions(String[] wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return (result.toArray(new String[result.size()]));
    }

    public enum Quality {
        LOW(0), HIGH(1);

        private final int value;

        private Quality(int value) {
            this.value = value;
        }

        int getValue() {
            return (value);
        }
    }

    abstract public static class IntentBuilder<T extends IntentBuilder> {
        abstract public Intent buildChooserBaseIntent();

        protected final Intent result;
        private final Context ctxt;

        /**
         * Standard constructor. May throw a runtime exception
         * if the environment is not set up properly (see
         * validateEnvironment() on Utils).
         *
         * @param ctxt any Context will do
         */
        public IntentBuilder(Context ctxt, Class clazz) {
            this.ctxt = ctxt.getApplicationContext();
            result = new Intent(ctxt, clazz);
        }

        /**
         * Returns the Intent defined by the builder.
         *
         * @return the Intent to use to start the activity
         */
        public Intent build() {
            Utils.validateEnvironment(ctxt,
                    result.getBooleanExtra(EXTRA_FAIL_IF_NO_PERMISSION, true));

            return (result);
        }

        /**
         * Returns an ACTION_CHOOSER Intent, to offer a choice
         * between this library's activity and existing camera
         * apps.
         *
         * @param title title for chooser dialog, or null
         * @return the Intent to use to start the activity
         */
        public Intent buildChooser(CharSequence title) {
            Intent original = build();

            Intent toChooseFrom = buildChooserBaseIntent();

            if (original.hasExtra(MediaStore.EXTRA_OUTPUT)) {
                toChooseFrom.putExtra(MediaStore.EXTRA_OUTPUT,
                        (Bundle) original.getParcelableExtra(MediaStore.EXTRA_OUTPUT));
            }

            Intent chooser = Intent.createChooser(toChooseFrom, title);

            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    new Intent[]{original});

            return (chooser);
        }

        /**
         * Indicates what camera should be used as the starting
         * point. Defaults to the rear-facing camera.
         *
         * @param facing which camera to use
         * @return the builder, for further configuration
         */
        public T facing(Facing facing) {
            result.putExtra(EXTRA_FACING, facing);

            return ((T) this);
        }

        /**
         * Indicates that the desired facing value for the camera
         * must be an exact match (and, if not, cancel the request).
         *
         * @return the builder, for further configuration
         */
        public T facingExactMatch() {
            result.putExtra(EXTRA_FACING_EXACT_MATCH, true);

            return ((T) this);
        }

        /**
         * Call if you want extra diagnostic information dumped to
         * LogCat. Not ideal for use in production.
         *
         * @return the builder, for further configuration
         */
        public T debug() {
            result.putExtra(EXTRA_DEBUG_ENABLED, true);

            return ((T) this);
        }

        /**
         * Indicates where to write the picture to. Defaults to
         * returning a thumbnail bitmap in the "data" extra, as
         * with ACTION_IMAGE_CAPTURE. Note that you need to have
         * write access to the supplied file.
         *
         * @param f file in which to write the picture
         * @return the builder, for further configuration
         */
        public T to(File f) {
            return ((T) to(Uri.fromFile(f)));
        }

        /**
         * Indicates where to write the picture to. Defaults to
         * returning a thumbnail bitmap in the "data" extra, as
         * with ACTION_IMAGE_CAPTURE. Note that you need to have
         * write access to the supplied Uri.
         *
         * @param output Uri to which to write the picture
         * @return the builder, for further configuration
         */
        public T to(Uri output) {
            result.putExtra(MediaStore.EXTRA_OUTPUT, output);

            return ((T) this);
        }

        /**
         * Indicates that the picture that is taken should be
         * passed over to MediaStore for indexing. By default,
         * this does not happen automatically and is the responsibility
         * of your app, should the image be reachable by MediaStore
         * in the first place. This setting is only relevant for file://
         * Uri values.
         *
         * @return the builder, for further configuration
         */
        public T updateMediaStore() {
            result.putExtra(EXTRA_UPDATE_MEDIA_STORE, true);

            return ((T) this);
        }

        /**
         * Forces the use of a specific engine based on its ID. Default
         * is an engine chosen by the device we are running on.
         *
         * @param engineId CLASSIC or CAMERA2
         * @return the builder, for further configuration
         */
        public T forceEngine(CameraEngine.ID engineId) {
            result.putExtra(EXTRA_FORCE_ENGINE, engineId);

            return ((T) this);
        }

        @Deprecated
        public T forceClassic() {
            return (forceEngine(CameraEngine.ID.CLASSIC));
        }

        /**
         * Horizontally flips or mirrors the preview images.
         *
         * @return the builder, for further configuration
         */
        public T mirrorPreview() {
            result.putExtra(EXTRA_MIRROR_PREVIEW, true);

            return ((T) this);
        }

        /**
         * Sets the desired focus mode. Default is CONTINUOUS.
         *
         * @return the builder, for further configuration
         */
        public T focusMode(FocusMode focusMode) {
            result.putExtra(EXTRA_FOCUS_MODE, focusMode);

            return ((T) this);
        }

        /**
         * Sets the desired flash mode. This is a suggestion; if
         * the device does not support this mode, the device default
         * behavior will be used.
         *
         * @param mode the desired flash mode
         * @return the builder, for further configuration
         */
        public T flashMode(FlashMode mode) {
            return (flashModes(new FlashMode[]{mode}));
        }

        /**
         * Sets the desired flash modes, in priority-first order
         * (the first flash mode will be used if supported, otherwise
         * the second flash mode will be used if supported, ...).
         * These are a suggestion; if none of these modes are supported,
         * the default device behavior will be used.
         *
         * @param modes the flash modes to try
         * @return the builder, for further configuration
         */
        public T flashModes(FlashMode[] modes) {
            return (flashModes(Arrays.asList(modes)));
        }

        /**
         * Sets the desired flash modes, in priority-first order
         * (the first flash mode will be used if supported, otherwise
         * the second flash mode will be used if supported, ...).
         * These are a suggestion; if none of these modes are supported,
         * the default device behavior will be used.
         *
         * @param modes the flash modes to try
         * @return the builder, for further configuration
         */
        public T flashModes(List<FlashMode> modes) {
            result.putExtra(EXTRA_FLASH_MODES,
                    new ArrayList<FlashMode>(modes));

            return ((T) this);
        }

        /**
         * Call if we should allow the user to change the flash mode
         * on the fly (if the camera supports it).
         */
        public T allowSwitchFlashMode() {
            result.putExtra(EXTRA_ALLOW_SWITCH_FLASH_MODE, true);

            return ((T) this);
        }

        /**
         * Indicates the video quality to use for recording this
         * video. Matches EXTRA_VIDEO_QUALITY, except uses an enum
         * for type safety. Note that this is also used for still
         * image quality, despite the name of the extra.
         *
         * @param q LOW or HIGH
         * @return the builder, for further configuration
         */
        public T quality(Quality q) {
            result.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, q.getValue());

            return ((T) this);
        }

        /**
         * Provides a ResultReceiver, which will be invoked on any
         * error that the library cannot handle itself.
         *
         * @param rr a ResultReceiver to get error information
         * @return the builder, for further configuration
         */
        public T onError(ResultReceiver rr) {
            result.putExtra(EXTRA_UNHANDLED_ERROR_RECEIVER, rr);

            return ((T) this);
        }

        /**
         * Specifies an OrientationLockMode to apply to the camera
         * operation.
         *
         * @param mode an OrientationLockMode value
         * @return the builder, for further configuration
         */
        public T orientationLockMode(OrientationLockMode mode) {
            result.putExtra(EXTRA_ORIENTATION_LOCK_MODE, mode);

            return ((T) this);
        }

        /**
         * Call to configure the ZoomStyle to be used. Default
         * is NONE.
         *
         * @return the builder, for further configuration
         */
        public T zoomStyle(ZoomStyle zoomStyle) {
            result.putExtra(EXTRA_ZOOM_STYLE, zoomStyle);

            return ((T) this);
        }

        /**
         * Call to request that the library request permissions from the
         * user, rather than that being handled by the app.
         *
         * @return the builder, for further configuration
         */
        public T requestPermissions() {
            result.putExtra(EXTRA_FAIL_IF_NO_PERMISSION, false);

            return ((T) this);
        }

        /**
         * Call to request that we show a "rule of thirds" grid over the camera
         * preview.
         *
         * @return the builder, for further configuration
         */
        public T showRuleOfThirdsGrid() {
            result.putExtra(EXTRA_SHOW_RULE_OF_THIRDS_GRID, true);

            return ((T) this);
        }
    }
}
