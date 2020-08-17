package net.yazeed44.imagepicker.util;

import android.content.Context;

import androidx.annotation.NonNull;

public class SinglePicker extends Picker {

    public final boolean cropAfterPick;
    public float aspectRatioX = -1;
    public float aspectRatioY = -1;

    protected SinglePicker(SinglePicker.SingleBuilder builder) {
        super(builder);
        cropAfterPick = builder.cropAfterPick;
        aspectRatioX = builder.aspectRatioX;
        aspectRatioY = builder.aspectRatioY;
    }


    public static final class SingleBuilder extends Builder {
        private boolean cropAfterPick;
        private float aspectRatioX = -1;
        private float aspectRatioY = -1;

        public SingleBuilder(Builder other) {
            super(other);
        }

        public SingleBuilder(Context context, PickListener listener) {
            super(context, listener);
        }

        public SingleBuilder(@NonNull Context context, @NonNull PickListener listener, int themeResId) {
            super(context, listener, themeResId);
        }

        public SinglePicker.SingleBuilder setCropAfterPick(boolean cropAfterPick) {
            this.cropAfterPick = cropAfterPick;
            return this;
        }

        public SinglePicker.SingleBuilder withAspectRatio(float x, float y) {
            aspectRatioX = x;
            aspectRatioY = y;
            return this;
        }

        @Override
        public Picker build() {
            singleImage();
            return new SinglePicker(this);
        }
    }
}
