package net.yazeed44.imagepicker.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yazeed44 on 6/14/15.
 */
public class ImageEntry implements Serializable {
    public final int imageId;
    public final String path;
    public long dateTakenUnixTime;
    public boolean isPicked = false;
    public boolean isVideo = false;

    public ImageEntry(final Builder builder) {
        this.path = builder.mPath;
        this.imageId = builder.mImageId;
        this.dateTakenUnixTime = builder.dateTakenUnixTime;
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
        private long dateTakenUnixTime;

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
            final int dateTakenColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

            final int imageId = cursor.getInt(imageIdColumn);
            final String path = cursor.getString(dataColumn);
            final long dateTaken = cursor.getLong(dateTakenColumn);

            return new ImageEntry.Builder(path)
                    .imageId(imageId)
                    ;

        }


        public Builder imageId(int imageId) {
            this.mImageId = imageId;
            return this;
        }

        public Builder dateTaken(long timestamp) {
            this.dateTakenUnixTime = timestamp;
            return this;
        }


        public ImageEntry build() {
            return new ImageEntry(this);
        }


    }

}
