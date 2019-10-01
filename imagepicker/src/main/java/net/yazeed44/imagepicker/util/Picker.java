package net.yazeed44.imagepicker.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import net.yazeed44.imagepicker.data.Events;
import net.yazeed44.imagepicker.data.model.ImageEntry;
import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.ui.PickerActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by yazeed44
 * on 6/14/15.
 */
public final class Picker {

    public final int limit;
    public final WeakReference<Context> context;
    public final int fabBackgroundColor;
    public final int fabBackgroundColorWhenPressed;
    public final int imageBackgroundColorWhenChecked;
    public final int imageBackgroundColor;
    public final int imageCheckColor;
    public final int checkedImageOverlayColor;
    public final PickListener pickListener;
    public final int albumImagesCountTextColor;
    public final int albumBackgroundColor;
    public final int albumNameTextColor;
    public final PickMode pickMode;
    public final int themeResId;
    public final int popupThemeResId;
    public final int captureItemIconTintColor;
    public final int doneFabIconTintColor;
    public final boolean shouldShowCaptureMenuItem;
    public final int checkIconTintColor;
    public final boolean videosEnabled;
    public final int videoLengthLimit;
    public final int videoThumbnailOverlayColor;
    public final int videoIconTintColor;
    public final boolean backBtnInMainActivity;
    public final boolean shouldShowItemAfterPick;
    public float aspectRatioX = -1;
    public float aspectRatioY = -1;

    private Picker(final Builder builder) {
        context = new WeakReference<>(builder.mContext);
        limit = builder.mLimit;
        fabBackgroundColor = builder.mFabBackgroundColor;
        fabBackgroundColorWhenPressed = builder.mFabBackgroundColorWhenPressed;
        imageBackgroundColorWhenChecked = builder.mImageBackgroundColorWhenChecked;
        imageBackgroundColor = builder.mImageBackgroundColor;
        imageCheckColor = builder.mImageCheckColor;
        checkedImageOverlayColor = builder.mCheckedImageOverlayColor;
        pickListener = builder.mPickListener;
        albumBackgroundColor = builder.mAlbumBackgroundColor;
        albumImagesCountTextColor = builder.mAlbumImagesCountTextColor;
        albumNameTextColor = builder.mAlbumNameTextColor;
        pickMode = builder.mPickMode;
        themeResId = builder.mThemeResId;
        popupThemeResId = builder.mPopupThemeResId;
        captureItemIconTintColor = builder.mCaptureItemIconTintColor;
        doneFabIconTintColor = builder.mDoneFabIconTintColor;
        shouldShowCaptureMenuItem = builder.mShouldShowCaptureMenuItem;
        checkIconTintColor = builder.mCheckIconTintColor;
        videosEnabled = builder.mVideosEnabled;
        videoLengthLimit = builder.mVideoLengthLimit;
        videoThumbnailOverlayColor = builder.mVideoThumbnailOverlayColor;
        videoIconTintColor = builder.mVideoIconTintColor;
        backBtnInMainActivity = builder.mBackBtnInMainActivity;
        shouldShowItemAfterPick = builder.shouldShowItemAfterPick;
        aspectRatioX = builder.aspectRatioX;
        aspectRatioY = builder.aspectRatioY;
    }

    public void startActivity() {
        if (context.get() != null) {
            EventBus.getDefault().postSticky(new Events.OnPublishPickOptionsEvent(this));
            final Intent intent = new Intent(context.get(), PickerActivity.class);
            PickerActivity.mPickOptions = this;
            context.get().startActivity(intent);
        }
    }


    public enum PickMode {
        SINGLE_IMAGE, MULTIPLE_IMAGES, SINGLE_VIDEO, MULTIPLE_VIDEOS, MIX
    }

    public interface PickListener {
        void onPickedSuccessfully(final ArrayList<ImageEntry> images);

        void onCancel();

    }

    public static final class Builder {

        private final Context mContext;
        private final PickListener mPickListener;
        private final int mThemeResId;
        private boolean shouldShowItemAfterPick;
        private int mLimit = PickerActivity.NO_LIMIT;
        private int mFabBackgroundColor;
        @ColorInt
        private int mFabBackgroundColorWhenPressed;
        @ColorInt
        private int mImageBackgroundColorWhenChecked;
        @ColorInt
        private int mImageBackgroundColor;
        @ColorInt
        private int mImageCheckColor;
        @ColorInt
        private int mCheckedImageOverlayColor;
        @ColorInt
        private int mAlbumImagesCountTextColor;
        @ColorInt
        private int mAlbumBackgroundColor;
        @ColorInt
        private int mAlbumNameTextColor;
        private PickMode mPickMode;
        private int mPopupThemeResId;
        @ColorInt
        private int mDoneFabIconTintColor;
        @ColorInt
        private int mCaptureItemIconTintColor;
        private boolean mShouldShowCaptureMenuItem;
        @ColorInt
        private int mCheckIconTintColor;
        private boolean mVideosEnabled;
        private int mVideoLengthLimit;
        @ColorInt
        private int mVideoThumbnailOverlayColor;
        @ColorInt
        private int mVideoIconTintColor;
        private boolean mBackBtnInMainActivity=true;
        private float aspectRatioX = -1;
        private float aspectRatioY = -1;

        public Builder(final Context context, final PickListener listener) {
            mThemeResId = R.style.PickerTheme;
            mContext = context;
            mContext.setTheme(mThemeResId);
            mPickListener = listener;
            init();


        }

        public Builder(@NonNull final Context context, @NonNull final PickListener listener, @StyleRes final int themeResId) {
            mContext = context;
            mContext.setTheme(themeResId);
            mPickListener = listener;
            mThemeResId = themeResId;
            init();

        }

        private void init() {
            final TypedValue typedValue = new TypedValue();
            initUsingColorAccent(typedValue);

            mImageBackgroundColor = getColor(R.color.alter_unchecked_image_background);
            mImageCheckColor = getColor(R.color.alter_image_check_color);
            mCheckedImageOverlayColor = getColor(R.color.alter_checked_photo_overlay);
            mAlbumBackgroundColor = getColor(R.color.alter_album_background);
            mAlbumNameTextColor = getColor(R.color.alter_album_name_text_color);
            mAlbumImagesCountTextColor = getColor(R.color.alter_album_images_count_text_color);
            mFabBackgroundColorWhenPressed = ColorUtils.setAlphaComponent(mFabBackgroundColor, (int) (android.graphics.Color.alpha(mFabBackgroundColor) * 0.8f));
            mPickMode = PickMode.MULTIPLE_IMAGES;

            mPopupThemeResId = Util.getDefaultPopupTheme(mContext);
            mCaptureItemIconTintColor = mDoneFabIconTintColor = Util.getDefaultIconTintColor(mContext);

            mShouldShowCaptureMenuItem = true;

            mCheckIconTintColor = Color.WHITE;
            mVideosEnabled = false;
            mVideoLengthLimit = 0; // No limit

            mVideoThumbnailOverlayColor = getColor(R.color.alter_video_thumbnail_overlay);
            mVideoIconTintColor = Color.WHITE;
        }

        private int getColor(@ColorRes final int colorRes) {
            return ContextCompat.getColor(mContext, colorRes);
        }

        private void initUsingColorAccent(final TypedValue typedValue) {
            mContext.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
            mImageBackgroundColorWhenChecked = mFabBackgroundColor = typedValue.data;
        }

        /**
         * @param limit limit for the count of image user can pick, By default it's infinite
         */
        public Picker.Builder setLimit(final int limit) {
            mLimit = limit;
            return this;
        }

        public Picker.Builder withAspectRatio(float x, float y) {
            aspectRatioX = x;
            aspectRatioY = y;
            return this;
        }

        public Picker.Builder setShowItemAfterPick(final boolean isShow) {
            shouldShowItemAfterPick = isShow;
            return this;
        }

        public Picker.Builder setFabBackgroundColor(@ColorInt final int color) {
            mFabBackgroundColor = color;
            return this;
        }

        public Picker.Builder setFabBackgroundColorWhenPressed(@ColorInt final int color) {
            mFabBackgroundColorWhenPressed = color;
            return this;
        }

        public Picker.Builder setImageBackgroundColorWhenChecked(@ColorInt final int color) {
            mImageBackgroundColorWhenChecked = color;
            return this;
        }

        public Picker.Builder setImageBackgroundColor(@ColorInt final int color) {
            mImageBackgroundColor = color;
            return this;
        }

        public Picker.Builder setImageCheckColor(@ColorInt final int color) {
            mImageCheckColor = color;
            return this;
        }

        public Picker.Builder setCheckedImageOverlayColor(@ColorInt final int color) {
            mCheckedImageOverlayColor = color;
            return this;
        }


        public Picker.Builder setAlbumBackgroundColor(@ColorInt final int color) {
            mAlbumBackgroundColor = color;
            return this;
        }

        public Picker.Builder setAlbumNameTextColor(@ColorInt final int color) {
            mAlbumNameTextColor = color;
            return this;
        }

        public Picker.Builder setAlbumImagesCountTextColor(@ColorInt final int color) {
            mAlbumImagesCountTextColor = color;
            return this;
        }

        public Picker.Builder setPickMode(final PickMode pickMode) {
            mPickMode = pickMode;
            return this;
        }

        public Picker.Builder setToolbarPopupTheme(@StyleRes final int themeRes) {
            mPopupThemeResId = themeRes;
            return this;
        }

        public Picker.Builder setDoneFabIconTintColor(@ColorInt final int color) {
            mDoneFabIconTintColor = color;
            return this;
        }

        public Picker.Builder setCaptureItemIconTintColor(@ColorInt final int color) {
            mCaptureItemIconTintColor = color;
            return this;
        }

        public Picker.Builder disableCaptureImageFromCamera() {
            mShouldShowCaptureMenuItem = false;
            return this;
        }

        public Picker.Builder setCheckIconTintColor(@ColorInt final int color) {
            mCheckIconTintColor = color;
            return this;
        }

        public Picker.Builder setBackBtnInMainActivity(final boolean backBtn) {
            mBackBtnInMainActivity = backBtn;
            return this;
        }

        public Picker.Builder setVideosEnabled(final boolean enabled) {
            mVideosEnabled = enabled;
            return this;
        }

        public Picker.Builder setVideoLengthLimitInMilliSeconds(final int limit) {
            mVideoLengthLimit = limit;
            return this;
        }

        public Picker.Builder setVideoThumbnailOverlayColor(@ColorInt final int color) {
            mVideoThumbnailOverlayColor = color;
            return this;
        }

        public Picker.Builder setVideoIconTintColor(@ColorInt final int color) {
            mVideoIconTintColor = color;
            return this;
        }

        public Picker build() {
            return new Picker(this);
        }
    }
}