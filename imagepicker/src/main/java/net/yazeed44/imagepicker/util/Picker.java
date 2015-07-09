package net.yazeed44.imagepicker.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.util.TypedValue;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.ui.PickerActivity;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by yazeed44 on 6/14/15.
 */
public final class Picker {

    public final int limit;
    public final Context context;
    public final int actionBarBackgroundColor;
    public final int captureIconTintColor;
    public final int fabBackgroundColor;
    public final int fabBackgroundColorWhenPressed;
    public final int imageBackgroundColorWhenChecked;
    public final int imageBackgroundColor;
    public final int imageCheckColor;
    public final int checkedImageOverlayColor;
    public final PickListener pickListener;
    public final int doneIconTintColor;
    public final int albumImagesCountTextColor;
    public final int albumBackgroundColor;
    public final int albumNameTextColor;
    public final PickMode pickMode;
    public final int themeResId;


    private Picker(final Builder builder) {
        context = builder.mContext;
        limit = builder.mLimit;
        actionBarBackgroundColor = builder.mActionBarBackgroundColor;
        captureIconTintColor = builder.mCaptureIconTintColor;
        fabBackgroundColor = builder.mFabBackgroundColor;
        fabBackgroundColorWhenPressed = builder.mFabBackgroundColorWhenPressed;
        imageBackgroundColorWhenChecked = builder.mImageBackgroundColorWhenChecked;
        imageBackgroundColor = builder.mImageBackgroundColor;
        imageCheckColor = builder.mImageCheckColor;
        checkedImageOverlayColor = builder.mCheckedImageOverlayColor;
        pickListener = builder.mPickListener;
        doneIconTintColor = builder.mDoneIconTintColor;
        albumBackgroundColor = builder.mAlbumBackgroundColor;
        albumImagesCountTextColor = builder.mAlbumImagesCountTextColor;
        albumNameTextColor = builder.mAlbumNameTextColor;
        pickMode = builder.mPickMode;
        themeResId = builder.mThemeResId;


    }

    public void startActivity() {

        EventBus.getDefault().postSticky(new Events.OnPublishPickOptionsEvent(this));

        final Intent intent = new Intent(context, PickerActivity.class);

        context.startActivity(intent);

    }


    public enum PickMode {

        SINGLE_IMAGE, MULTIPLE_IMAGES
    }

    public interface PickListener {
        void onPickedSuccessfully(final ArrayList<ImageEntry> images);

        void onCancel();

    }

    public static class Builder {

        private final Context mContext;
        private final PickListener mPickListener;
        private int mDoneIconTintColor;
        private int mLimit = PickerActivity.NO_LIMIT;
        private int mActionBarBackgroundColor;
        private int mCaptureIconTintColor;

        private int mFabBackgroundColor;
        private int mFabBackgroundColorWhenPressed;

        private int mImageBackgroundColorWhenChecked;
        private int mImageBackgroundColor;
        private int mImageCheckColor;
        private int mCheckedImageOverlayColor;
        private int mAlbumImagesCountTextColor;
        private int mAlbumBackgroundColor;
        private int mAlbumNameTextColor;
        private PickMode mPickMode;
        private int mThemeResId;


        //Use (Context,PickListener,themeResId) instead
        @Deprecated
        public Builder(final Context context, final PickListener listener) {

            mContext = context;
            mPickListener = listener;
            init();


        }

        public Builder(final Context context, final PickListener listener, final int themeResId) {
            mContext = context;
            mContext.setTheme(themeResId);
            mPickListener = listener;
            mThemeResId = themeResId;
            init();

        }

        private void init() {
            final TypedValue typedValue = new TypedValue();
            initUsingColorPrimary(typedValue);
            initUsingColorAccent(typedValue);
            initUsingActionMenuTextColor(typedValue);


            mImageBackgroundColor = mContext.getResources().getColor(R.color.alter_unchecked_image_background);
            mImageCheckColor = mContext.getResources().getColor(R.color.alter_image_check_color);
            mCheckedImageOverlayColor = mContext.getResources().getColor(R.color.alter_checked_photo_overlay);
            mAlbumBackgroundColor = mContext.getResources().getColor(R.color.alter_album_background);
            mAlbumNameTextColor = mContext.getResources().getColor(R.color.alter_album_name_text_color);
            mAlbumImagesCountTextColor = mContext.getResources().getColor(R.color.alter_album_images_count_text_color);

            mFabBackgroundColorWhenPressed = ColorUtils.setAlphaComponent(mFabBackgroundColor, (int) (android.graphics.Color.alpha(mFabBackgroundColor) * 0.8f));

            mPickMode = PickMode.MULTIPLE_IMAGES;

            if (mThemeResId == 0)
                initUsingThemeResId(typedValue);


        }

        private void initUsingColorPrimary(final TypedValue typedValue) {

            if (mContext.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true)) {
                mActionBarBackgroundColor = typedValue.data;

            } else {

                mActionBarBackgroundColor = mContext.getResources().getColor(R.color.alter_primary);

            }
        }

        private void initUsingColorAccent(final TypedValue typedValue) {


            if (mContext.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true)) {

                mImageBackgroundColorWhenChecked = mFabBackgroundColor = typedValue.data;
            } else {
                mImageBackgroundColorWhenChecked = mFabBackgroundColor = mContext.getResources().getColor(R.color.alter_checked_image_background);
            }


        }

        private void initUsingActionMenuTextColor(final TypedValue typedValue) {


            if (mContext.getTheme().resolveAttribute(R.attr.itemTextColor, typedValue, true)) {

                mCaptureIconTintColor = typedValue.data;
                mDoneIconTintColor = typedValue.data;


            } else {
                mCaptureIconTintColor = Color.GRAY;
                mDoneIconTintColor = Color.GRAY;
            }
        }

        private void initUsingThemeResId(final TypedValue typedValue) {

            if (mContext.getTheme().resolveAttribute(R.attr.theme, typedValue, true)) {
                //Initialize default theme
                mThemeResId = typedValue.resourceId;

            } else {

                mThemeResId = R.style.Theme_AppCompat_Light_NoActionBar;
            }
        }

        /**
         * @param limit limit for the count of image user can pick , By default it's infinite
         */
        public Picker.Builder setLimit(final int limit) {
            mLimit = limit;
            return this;
        }


        public Picker.Builder setCaptureIconTintColor(final int color) {
            mCaptureIconTintColor = color;
            return this;
        }

        public Picker.Builder setDoneIconTintColor(final int color) {
            mDoneIconTintColor = color;
            return this;
        }

        public Picker.Builder setActionBarBackgroundColor(final int color) {
            mActionBarBackgroundColor = color;
            return this;
        }

        public Picker.Builder setFabBackgroundColor(final int color) {
            mFabBackgroundColor = color;
            return this;
        }

        public Picker.Builder setFabBackgroundColorWhenPressed(final int color) {
            mFabBackgroundColorWhenPressed = color;
            return this;
        }

        public Picker.Builder setImageBackgroundColorWhenChecked(final int color) {
            mImageBackgroundColorWhenChecked = color;
            return this;
        }

        public Picker.Builder setImageBackgroundColor(final int color) {
            mImageBackgroundColor = color;
            return this;
        }

        public Picker.Builder setImageCheckColor(final int color) {
            mImageCheckColor = color;
            return this;
        }

        public Picker.Builder setCheckedImageOverlayColor(final int color) {
            mCheckedImageOverlayColor = color;
            return this;
        }


        public Picker.Builder setAlbumBackgroundColor(final int color) {
            mAlbumBackgroundColor = color;
            return this;
        }

        public Picker.Builder setAlbumNameTextColor(final int color) {
            mAlbumNameTextColor = color;
            return this;
        }

        public Picker.Builder setAlbumImagesCountTextColor(final int color) {
            mAlbumImagesCountTextColor = color;
            return this;
        }

        public Picker.Builder setPickMode(final PickMode pickMode) {
            mPickMode = pickMode;
            return this;
        }

        public Picker build() {
            return new Picker(this);
        }


    }
}
