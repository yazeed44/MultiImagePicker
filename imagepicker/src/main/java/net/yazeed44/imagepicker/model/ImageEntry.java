package net.yazeed44.imagepicker.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.Serializable;

/**
 * Created by yazeed44 on 6/14/15.
 */
public class ImageEntry implements Serializable {
    public final int imageId;
    public final String path;
    public boolean isPicked = false;

    public ImageEntry(final Builder builder) {
        this.path = builder.mPath;
        this.imageId = builder.mImageId;
    }

    public static ImageEntry from(final Cursor cursor) {
        return Builder.from(cursor).build();
    }

    public static ImageEntry from(final Uri uri) {
        return Builder.from(uri).build();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageEntry && imageId == ((ImageEntry) o).imageId;
    }

    @Override
    public String toString() {
        return "ImageEntry{" +
                "path='" + path + '\'' +
                '}';
    }

    public static class Builder {

        public static int count = -1;
        private final String mPath;
        private int mImageId;

        public Builder(final String path) {
            this.mPath = path;
        }

        public static Builder from(final Uri uri) {

            return new Builder(uri.getPath())
                    .imageId(count--)
                    ;

        }

        public static Builder from(final Cursor cursor) {
            final int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            final int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);

            final int imageId = cursor.getInt(imageIdColumn);
            final String path = cursor.getString(dataColumn);

            return new ImageEntry.Builder(path)
                    .imageId(imageId)
                    ;

        }


        public Builder imageId(int imageId) {
            this.mImageId = imageId;
            return this;
        }


        public ImageEntry build() {
            return new ImageEntry(this);
        }


    }

}
