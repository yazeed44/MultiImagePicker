package net.yazeed44.imagepicker.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yalantis.ucrop.UCrop;

import net.yazeed44.imagepicker.data.Events;
import net.yazeed44.imagepicker.data.model.AlbumEntry;
import net.yazeed44.imagepicker.data.model.ImageEntry;
import net.yazeed44.imagepicker.library.BuildConfig;
import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.ui.album.AlbumsFragment;
import net.yazeed44.imagepicker.ui.imagePreview.ImagePreviewActivity;
import net.yazeed44.imagepicker.ui.photo.ImagesThumbnailFragment;
import net.yazeed44.imagepicker.ui.photoPager.ImagesPagerFragment;
import net.yazeed44.imagepicker.util.CameraSupport;
import net.yazeed44.imagepicker.util.LocaleHelper;
import net.yazeed44.imagepicker.util.Picker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PickerActivity extends AppCompatActivity {
    public static final int NO_LIMIT = -1;
    public static final String KEY_ACTION_BAR_TITLE = "actionBarKey";
    public static final String KEY_SHOULD_SHOW_ACTIONBAR_UP = "shouldShowUpKey";
    public static final String CAPTURED_IMAGES_ALBUM_NAME = "Captured";
    public static final String CAPTURED_IMAGES_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    private static final int REQUEST_PORTRAIT_RFC = 1337;
    public static final int REQUEST_PORTRAIT_FFC = REQUEST_PORTRAIT_RFC + 1;
    public static ArrayList<ImageEntry> sCheckedImages = new ArrayList<>();
    private boolean mShouldShowUp = false;
    private FloatingActionButton mDoneFab;
    public static Picker mPickOptions;
    //For ViewPager
    private ImageEntry mCurrentlyDisplayedImage;
    private AlbumEntry mSelectedAlbum;
    private MenuItem mSelectAllMenuItem;
    private MenuItem mDeselectAllMenuItem;
    Toolbar toolbar;


    //TODO Add support for gif
    //TODO Use robust method for capturing photos
    //TODO Add support for picking videos
    //TODO When photo is captured a new album created for it

    @Subscribe(sticky = true)
    public void onEvent(Events.OnPublishPickOptionsEvent event) {
        if (mPickOptions == null)
            mPickOptions = event.options;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
        if (mPickOptions == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_pick);
        initTheme();
        toolbar = findViewById(R.id.album_toolbar);
        addToolbarToLayout();
        initActionbar(savedInstanceState);
        setupAlbums(savedInstanceState);
        initFab();
        updateFab();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null)
            outState.putString(KEY_ACTION_BAR_TITLE, getSupportActionBar().getTitle().toString());
        outState.putBoolean(KEY_SHOULD_SHOW_ACTIONBAR_UP, mShouldShowUp);
    }

    private void initTheme() {
        setTheme(mPickOptions.themeResId);
    }

    private void addToolbarToLayout() {
        toolbar.setTitle("Chọn ảnh");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(AlbumsFragment.TAG);
        if (fragment instanceof AlbumsFragment) {
            ((AlbumsFragment) fragment).setupRecycler();
        } else {
            fragment = getSupportFragmentManager().findFragmentByTag(ImagesThumbnailFragment.TAG);
            if (fragment instanceof ImagesThumbnailFragment) {
                ((ImagesThumbnailFragment) fragment).setupRecycler();
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    private void initActionbar(final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mShouldShowUp = mPickOptions.backBtnInMainActivity;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(mPickOptions.backBtnInMainActivity);
                getSupportActionBar().setTitle(R.string.albums_title);
            }
        } else {
            mShouldShowUp = savedInstanceState.getBoolean(KEY_SHOULD_SHOW_ACTIONBAR_UP);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(mShouldShowUp && mPickOptions.backBtnInMainActivity);
                getSupportActionBar().setTitle(savedInstanceState.getString(KEY_ACTION_BAR_TITLE));
            }
        }
    }

    public void initFab() {
        Drawable doneIcon = AppCompatResources.getDrawable(this, R.drawable.ic_action_done_white);
        if (doneIcon != null) {
            doneIcon = DrawableCompat.wrap(doneIcon);
            DrawableCompat.setTint(doneIcon, mPickOptions.doneFabIconTintColor);
        }
        mDoneFab = findViewById(R.id.fab_done);
        mDoneFab.setImageDrawable(doneIcon);
        mDoneFab.setBackgroundTintList(ColorStateList.valueOf(mPickOptions.fabBackgroundColor));
        mDoneFab.setRippleColor(mPickOptions.fabBackgroundColorWhenPressed);

        EventBus.getDefault().postSticky(new Events.OnAttachFabEvent(mDoneFab));


    }

    public void setupAlbums(Bundle savedInstanceState) {
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState == null) {
                AlbumsFragment albumsFragment = new AlbumsFragment();
                albumsFragment.setmPickOptions(mPickOptions);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_up_enter,
                                R.anim.slide_left_exit,
                                R.anim.slide_down_enter,
                                R.anim.slide_right_exit)
                        .add(R.id.fragment_container, albumsFragment, AlbumsFragment.TAG)
                        .commit();
            }
        }
    }


    public void updateFab() {
        if (mPickOptions.pickMode == Picker.PickMode.SINGLE_IMAGE) {
            mDoneFab.hide();
            return;
        }
        if (sCheckedImages.size() == 0) {
            mDoneFab.hide();
        } else if (sCheckedImages.size() == mPickOptions.limit) {
            //Might change FAB appearance on other version
            mDoneFab.show();
            mDoneFab.bringToFront();
        } else {
            mDoneFab.show();
            mDoneFab.bringToFront();
        }
    }

    public void onClickDone(View view) {
        if (mPickOptions.pickMode == Picker.PickMode.SINGLE_IMAGE) {

            sCheckedImages.add(mCurrentlyDisplayedImage);
            mCurrentlyDisplayedImage.isPicked = true;
        }
        //Don't need to modify sCheckedImages for Multiple images mode


        //New object because sCheckedImages will get cleared
        if (sCheckedImages == null || sCheckedImages.isEmpty()) {
            super.finish();
            onCancel();
        } else {
            if (mPickOptions.shouldShowItemAfterPick) {
                if (mPickOptions.pickMode == Picker.PickMode.SINGLE_IMAGE) {
                    Uri destinationUri = Uri.fromFile(new File(this.getCacheDir(), "IMG_" + System.currentTimeMillis()));

                    UCrop builder = UCrop.of(mCurrentlyDisplayedImage.getUri(), destinationUri);
                    if (mPickOptions != null && mPickOptions.aspectRatioY != -1 && mPickOptions.aspectRatioY != -1)
                        builder.withAspectRatio(mPickOptions.aspectRatioX, mPickOptions.aspectRatioY);
                    builder.start(this);
//                    super.finish();
                } else {
                    EventBus.getDefault().postSticky(new Events.OnPublishPickOptionsEvent(mPickOptions));
                    EventBus.getDefault().postSticky(new Events.OnImagePreviewEvent(sCheckedImages));
                    Intent intent = new Intent(this, ImagePreviewActivity.class);
                    startActivityForResult(intent, ImagePreviewActivity.REQUEST_PREVIEW);
                }
            } else {
                super.finish();
                mPickOptions.pickListener.onPickedSuccessfully(new ArrayList<>(sCheckedImages));
                sCheckedImages.clear();
                EventBus.getDefault().removeAllStickyEvents();
            }
        }
    }

    @Subscribe
    public void onEvent(Events.OnPublishPreview e) {
        // Log.d(getClass().getSimpleName(), "onEvent() called with: e = [" + e.imageEntries.get(0).getDescription() + "]");
        sCheckedImages = e.imageEntries;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PORTRAIT_FFC) {
            if (resultCode == RESULT_OK) {
                //For capturing image from camera
                if (captureImageFile != null)
                    refreshMediaScanner(captureImageFile.getPath());
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                final Uri resultUri = UCrop.getOutput(data);
                ImageEntry imageEntry = ImageEntry.from(resultUri);
                List<ImageEntry> imageEntries = new ArrayList<>();
                imageEntries.add(imageEntry);
                super.finish();
                mPickOptions.pickListener.onPickedSuccessfully(new ArrayList<>(imageEntries));
                sCheckedImages.clear();
                EventBus.getDefault().removeAllStickyEvents();
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
            }
        } else if (requestCode == ImagePreviewActivity.REQUEST_PREVIEW) {
            if (resultCode == RESULT_OK) {
                super.finish();
                mPickOptions.pickListener.onPickedSuccessfully(new ArrayList<>(sCheckedImages));
                sCheckedImages.clear();
                EventBus.getDefault().removeAllStickyEvents();
            } else {

            }
        }

    }

    public void onCancel() {
        mPickOptions.pickListener.onCancel();
        sCheckedImages.clear();
        EventBus.getDefault().removeAllStickyEvents();
    }

    public void startCamera() {
        if (!CameraSupport.isEnabled()) {
            return;
        }

        if (!mPickOptions.videosEnabled) {
            capturePhoto();
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.dialog_choose_camera_title)
                .setItems(new String[]{getResources().getString(R.string.dialog_choose_camera_item_0), getResources().getString(R.string.dialog_choose_camera_item_1)}, (dialog, which) -> {
                            if (which == 0) {
                                capturePhoto();
                            } else {
                                captureVideo();
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static final int MY_CAMERA_PERMISSION_CODE = 100;
    File captureImageFile;
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    public static boolean verifyStoragePermissions(Activity activity) {
        if (activity == null) return false;
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED || permission1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        } else return true;
    }

    public void capturePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_PERMISSION_CODE);
        } else {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            captureImageFile = createTemporaryFileForCapturing(".png");
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(captureImageFile));
            startActivityForResult(i, REQUEST_PORTRAIT_FFC);
        }
    }

    public static File createTemporaryFileForCapturing(final String extension) {
        final File captureTempFile = new File(CAPTURED_IMAGES_DIR
                + "/IMG_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
                + extension);
        try {
            captureTempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("capture", e.getMessage());
        }
        return captureTempFile;
    }

    private void logException(Throwable e) {
        if (BuildConfig.DEBUG) e.printStackTrace();
    }

    public void captureVideo() {
        final File captureVideoFile = createTemporaryFileForCapturing(".mp4");
        CameraSupport.startVideoCaptureActivity(this,
                captureVideoFile, mPickOptions.videoLengthLimit, REQUEST_PORTRAIT_FFC);
    }


    private void refreshMediaScanner(final String imagePath) {
        MediaScannerConnection.scanFile(this,
                new String[]{imagePath}, null,
                (path, uri) -> {
                    PickerActivity.this.runOnUiThread(this::reloadAlbums);
                    // Log.d("onActivityResult", "New image should appear in camera folder");
                });
    }

    private void reloadAlbums() {
        if (isImagesThumbnailShown()) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            getSupportFragmentManager().popBackStackImmediate(ImagesThumbnailFragment.TAG, 0);
            getSupportFragmentManager().popBackStackImmediate();
        }
        EventBus.getDefault().post(new Events.OnReloadAlbumsEvent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mPickOptions.shouldShowCaptureMenuItem) {
            initCaptureMenuItem(menu);
        }

        getMenuInflater().inflate(R.menu.menu_select_all, menu);
        getMenuInflater().inflate(R.menu.menu_deselect_all, menu);


        mSelectAllMenuItem = menu.findItem(R.id.action_select_all);
        mDeselectAllMenuItem = menu.findItem(R.id.action_deselect_all);

        if (shouldShowDeselectAll()) {
            showDeselectAll();
        } else {
            hideDeselectAll();
        }

        if (shouldShowSelectAll()) {
            showSelectAll();
        } else {
            hideSelectAll();
        }

        return true;
    }

    private boolean shouldShowSelectAll() {
        final Fragment imageThumbnailFragment = getSupportFragmentManager().findFragmentByTag(ImagesThumbnailFragment.TAG);
        return imageThumbnailFragment != null && imageThumbnailFragment.isVisible() && !mPickOptions.pickMode.equals(Picker.PickMode.SINGLE_IMAGE);
    }

    private void initCaptureMenuItem(final Menu menu) {
        if (CameraSupport.isEnabled()) {
            getMenuInflater().inflate(R.menu.menu_take_photo, menu);
            Drawable captureIconDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_action_camera_white);
            captureIconDrawable = DrawableCompat.wrap(captureIconDrawable);
            DrawableCompat.setTint(captureIconDrawable, mPickOptions.captureItemIconTintColor);
            menu.findItem(R.id.action_take_photo).setIcon(captureIconDrawable);
        }
    }

    private void hideDeselectAll() {
        mDeselectAllMenuItem.setVisible(false);

    }

    private void showDeselectAll() {

        if (mPickOptions.pickMode == Picker.PickMode.SINGLE_IMAGE) {
            return;
        }

        mDeselectAllMenuItem.setVisible(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity_album in AndroidManifest.xml.
        final int itemId = item.getItemId();
        if (itemId == R.id.action_take_photo) {
            startCamera();
        } else if (itemId == R.id.action_select_all) {
            selectAllImages();

        } else if (itemId == R.id.action_deselect_all) {
            deselectAllImages();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deselectAllImages() {
        for (final ImageEntry imageEntry : mSelectedAlbum.imageList) {
            imageEntry.isPicked = false;
            sCheckedImages.remove(imageEntry);
        }
        EventBus.getDefault().post(new Events.OnUpdateImagesThumbnailEvent());
        hideDeselectAll();
        updateFab();
    }

    private void selectAllImages() {
        if (mSelectedAlbum == null) {
            mSelectedAlbum = EventBus.getDefault().getStickyEvent(Events.OnClickAlbumEvent.class).albumEntry;
        }
        if (sCheckedImages.size() < mPickOptions.limit || mPickOptions.limit == NO_LIMIT) {
            for (final ImageEntry imageEntry : mSelectedAlbum.imageList) {
                if (mPickOptions.limit != NO_LIMIT && sCheckedImages.size() + 1 > mPickOptions.limit) {
                    //Hit the limit
                    Toast.makeText(this, R.string.you_cant_check_more_images, Toast.LENGTH_SHORT).show();
                    break;
                }

                if (!imageEntry.isPicked) {
                    //To avoid repeated images
                    sCheckedImages.add(imageEntry);
                    imageEntry.isPicked = true;
                }
            }
        }
        EventBus.getDefault().post(new Events.OnUpdateImagesThumbnailEvent());
        updateFab();

        if (shouldShowDeselectAll()) {
            showDeselectAll();
        }
    }


    @Override
    public void onBackPressed() {
        if (isImagesThumbnailShown()) {
            //Return to albums fragment
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setTitle(R.string.albums_title);
            mShouldShowUp = mPickOptions.backBtnInMainActivity;
            getSupportActionBar().setDisplayHomeAsUpEnabled(mShouldShowUp);
            hideSelectAll();
            hideDeselectAll();

        } else if (isImagesPagerShown()) {
            //Returns to images thumbnail fragment

            if (mSelectedAlbum == null) {
                mSelectedAlbum = EventBus.getDefault().getStickyEvent(Events.OnClickAlbumEvent.class).albumEntry;
            }
            mDoneFab.hide();
            getSupportFragmentManager().popBackStack(ImagesThumbnailFragment.TAG, 0);
            getSupportActionBar().setTitle(mSelectedAlbum.name);
            getSupportActionBar().show();
            showSelectAll();

        } else {
            //User on album fragment
            super.onBackPressed();
            onCancel();
        }
    }

    private boolean isImagesThumbnailShown() {
        return isFragmentShown(ImagesThumbnailFragment.TAG);
    }

    private boolean isImagesPagerShown() {
        return isFragmentShown(ImagesPagerFragment.TAG);
    }


    private boolean isFragmentShown(final String tag) {

        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        return fragment != null && fragment.isVisible();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();

        return true;
    }

    private void hideSelectAll() {
        mSelectAllMenuItem.setVisible(false);

    }

    private void showSelectAll() {
        if (mPickOptions.pickMode == Picker.PickMode.SINGLE_IMAGE) {
            //Keep it hidden
            return;
        }
        mSelectAllMenuItem.setVisible(true);

    }

    private void handleMultipleModeAddition(final ImageEntry imageEntry) {
        if (mPickOptions.pickMode != Picker.PickMode.MULTIPLE_IMAGES) {
            return;
        }

        if (sCheckedImages.size() < mPickOptions.limit || mPickOptions.limit == NO_LIMIT) {
            imageEntry.isPicked = true;
            sCheckedImages.add(imageEntry);
        } else {

            Toast.makeText(this, String.format(Locale.getDefault(), getString(R.string.can_choose_d_image), mPickOptions.limit), Toast.LENGTH_SHORT).show();
            Log.i("onPickImage", "You can't check more images");
        }
    }

    private boolean shouldShowDeselectAll() {
        if (mSelectedAlbum == null) {
            return false;
        }

        boolean isAllImagesSelected = true;
        for (final ImageEntry albumChildImage : mSelectedAlbum.imageList) {

            if (!sCheckedImages.contains(albumChildImage)) {
                isAllImagesSelected = false;
                break;
            }
        }

        final Fragment imageThumbnailFragment = getSupportFragmentManager().findFragmentByTag(ImagesThumbnailFragment.TAG);

        return isAllImagesSelected && imageThumbnailFragment != null && imageThumbnailFragment.isVisible();
    }

    private void handleToolbarVisibility(final boolean show) {

        final AppBarLayout appBarLayout = (AppBarLayout) toolbar.getParent();
        final CoordinatorLayout rootLayout = (CoordinatorLayout) appBarLayout.getParent();

        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null)
            if (show) {
                //Show appBar
                behavior.setTopAndBottomOffset(0);
                behavior.onNestedPreScroll(rootLayout, appBarLayout, null, 0, 1, new int[2]);

            } else {
                //Hide appBar
                behavior.onNestedFling(rootLayout, appBarLayout, null, 0, 10000, true);
            }

    }


    @Subscribe(sticky = true)
    public void onEvent(final Events.OnClickAlbumEvent albumEvent) {
        mSelectedAlbum = albumEvent.albumEntry;


        ImagesThumbnailFragment imagesThumbnailFragment = (ImagesThumbnailFragment) getSupportFragmentManager().findFragmentByTag(ImagesThumbnailFragment.TAG);
        if (imagesThumbnailFragment == null) {
            imagesThumbnailFragment = new ImagesThumbnailFragment();
        }
        imagesThumbnailFragment.setmPickOptions(mPickOptions);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up_enter,
                        R.anim.slide_left_exit,
                        R.anim.slide_down_enter,
                        R.anim.slide_right_exit)
                .replace(R.id.fragment_container, imagesThumbnailFragment, ImagesThumbnailFragment.TAG)
                .addToBackStack(ImagesThumbnailFragment.TAG)
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(albumEvent.albumEntry.name);
            mShouldShowUp = true;
            getSupportActionBar().setDisplayHomeAsUpEnabled(mShouldShowUp);
        }
        showSelectAll();

        if (shouldShowDeselectAll()) {
            showDeselectAll();
        } else {
            hideDeselectAll();
        }


    }


    @Subscribe(sticky = true)
    public void onEvent(final Events.OnPickImageEvent pickImageEvent) {
        if (mPickOptions.videosEnabled && mPickOptions.videoLengthLimit > 0 && pickImageEvent.imageEntry.isVideo) {
            // Check to see if the selected video is too long in length
            final MediaPlayer mp = MediaPlayer.create(this, Uri.parse(pickImageEvent.imageEntry.path));
            final int duration = mp.getDuration();
            mp.release();
            if (duration > (mPickOptions.videoLengthLimit)) {
                Toast.makeText(this, getResources().getString(R.string.video_too_long).replace("$", String.valueOf(mPickOptions.videoLengthLimit / 1000)), Toast.LENGTH_SHORT).show();
                return; // Don't allow selection
            }
        }

        if (mPickOptions.pickMode == Picker.PickMode.MULTIPLE_IMAGES) {
            handleMultipleModeAddition(pickImageEvent.imageEntry);


        } else if (mPickOptions.pickMode == Picker.PickMode.SINGLE_IMAGE) {
            //Single image pick mode
            ImagesPagerFragment pagerFragment = (ImagesPagerFragment) getSupportFragmentManager().findFragmentByTag(ImagesPagerFragment.TAG);
            if (pagerFragment == null)
                pagerFragment = new ImagesPagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_enter,
                            R.anim.slide_left_exit,
                            R.anim.slide_down_enter,
                            R.anim.slide_right_exit)
                    .replace(R.id.fragment_container, pagerFragment, ImagesPagerFragment.TAG)
                    .addToBackStack(ImagesPagerFragment.TAG)
                    .commit();
        }
        updateFab();
    }


    @Subscribe
    public void onEvent(final Events.OnUnpickImageEvent unpickImageEvent) {
        sCheckedImages.remove(unpickImageEvent.imageEntry);
        unpickImageEvent.imageEntry.isPicked = false;

        updateFab();
        hideDeselectAll();
    }

    @Subscribe
    public void onEvent(final Events.OnChangingDisplayedImageEvent newImageEvent) {
        mCurrentlyDisplayedImage = newImageEvent.currentImage;

    }

    @Subscribe
    public void onEvent(final Events.OnShowingToolbarEvent showingToolbarEvent) {
        handleToolbarVisibility(true);
    }


    @Subscribe
    public void onEvent(final Events.OnHidingToolbarEvent hidingToolbarEvent) {
        handleToolbarVisibility(false);
    }


}
