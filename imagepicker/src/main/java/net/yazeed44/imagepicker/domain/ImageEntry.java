package net.yazeed44.imagepicker.domain;

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
        private final String mPath;
        private int mImageId;

        public Builder(final String path) {
            this.mPath = path;
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
