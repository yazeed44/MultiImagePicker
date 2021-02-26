package net.yazeed44.imagepicker.util;

import android.content.Context;

import androidx.annotation.NonNull;

public class MultiplePicker extends Picker {

    public final boolean inputDescription;
    public final String descriptionHint;

    protected MultiplePicker(MultipleBuilder builder) {
        super(builder);
        inputDescription = builder.inputDescription;
        descriptionHint = builder.descriptionHint;
    }

    public static final class MultipleBuilder extends Picker.Builder {
        private boolean inputDescription;
        private String descriptionHint;

        public MultipleBuilder(Context context, Picker.PickListener listener) {
            super(context, listener);
        }

        public MultipleBuilder(@NonNull Context context, @NonNull Picker.PickListener listener, int themeResId) {
            super(context, listener, themeResId);
        }

        public MultipleBuilder(Builder other) {
            super(other);
        }

        public MultiplePicker.MultipleBuilder setInputDescription(final boolean isShow) {
            inputDescription = isShow;
            return this;
        }

        public MultiplePicker.MultipleBuilder setDescriptionHint(final String descriptionHint) {
            this.descriptionHint = descriptionHint;
            return this;
        }

        @Override
        public Picker build() {
            multipleImage();
            return new MultiplePicker(this);
        }
    }
}
