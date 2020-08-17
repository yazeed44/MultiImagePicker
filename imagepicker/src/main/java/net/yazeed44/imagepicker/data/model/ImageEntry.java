package net.yazeed44.imagepicker.data.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

/**
 * Created by yazeed44
 * on 6/14/15.
 */
public class ImageEntry implements Parcelable {
    public final int imageId;
    public final String path;
    public final long dateAdded;
    public boolean isPicked = false;
    public boolean isVideo = false;

    private int orientation = 0;
    private int maxDimen = 1152;
    private int compressPercent = 100;
    private Bitmap bitmap;

    private Context context;
    private String description = "";
    private boolean uploaded;
    private int progress;


    private void logException(Throwable e) {
//        e.printStackTrace();
    }

    public ImageEntry(String path, Bitmap bitmap, Long dateAdded) {
        this.path = path;
        this.imageId = dateAdded.intValue();
        this.bitmap = bitmap;
        this.dateAdded = dateAdded;
    }

    public String getBase64(int maxSizeKB) {
        String base64 = getBase64();
        int size = base64.getBytes().length / 1024;
        while (size > maxSizeKB) {
            if (compressPercent > 70) {
                setCompressPercent(getCompressPercent() - 10);
                base64 = getBase64();
                size = base64.getBytes().length / 1024;
            } else break;
        }
        return base64;
    }

    public String getBase64() {
        String rt = "";
        Bitmap bmp = getScaledBitmap();
        ExifInterface exif;
        int angle = 0;
        try {
            exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logException(e);
        }
        Matrix matrix1 = new Matrix();

        //set image rotation value to 45 degrees in matrix.
        matrix1.postRotate(angle);
        if (bmp != null)
            //Create bitmap with new values.
            bmp = Bitmap.createBitmap(bmp, 0, 0,
                    bmp.getWidth(), bmp.getHeight(), matrix1, true);
        if (bmp != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, compressPercent, baos);
            byte[] b = baos.toByteArray();
            return Base64.encodeToString(b, Base64.DEFAULT);
        }
        return rt;
    }

    /**
     * Rotate a bitmap based on orientation metadata.
     * src - image path
     */
    public static Bitmap rotateBitmap(String src) {
        Bitmap bitmap = BitmapFactory.decodeFile(src);
        try {
            ExifInterface exif = new ExifInterface(src);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap getBitmapRotated() {
        Bitmap bmp = getScaledBitmap();
        ExifInterface exif;
        int angle = 0;
        try {
            exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);


            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logException(e);
        }
        Matrix matrix1 = new Matrix();

        //set image rotation value to 45 degrees in matrix.
        matrix1.postRotate(angle);
        //Create bitmap with new values.
        bmp = Bitmap.createBitmap(bmp, 0, 0,
                bmp.getWidth(), bmp.getHeight(), matrix1, true);
        return bmp;
    }

    public String getBase64Rotated(Context context) {
        String rt = "";
        Bitmap bmp = null;
        try {
            bmp = getBitmapResignedAndRotated(getUri(), context);
            if (bmp != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, compressPercent, baos);
                byte[] b = baos.toByteArray();
                return Base64.encodeToString(b, Base64.DEFAULT);
            }
        } catch (IOException e) {
            logException(e);
        }

        return rt;
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    public Bitmap getBitmapResignedAndRotated(Uri photoUri, Context context) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxDimen, maxDimen);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(photoUri);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, photoUri);
        return img;

    }

    public int getOrientation(Uri photoUri, Context mContext) {
        /* it's on the external media. */
        Cursor cursor = mContext.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);
        if (cursor == null) return -1;
        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean getUploaded() {
        return uploaded;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    public Bitmap decodeFile(File f) {
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), o);
//        Log.e("Test", "ABC");
        int original_width = o.outWidth;
        int original_height = o.outHeight;
        int new_width = original_width;
        int new_height = original_height;
        if (original_width > maxDimen) {
            new_width = maxDimen;
            new_height = (new_width * original_height) / original_width;
        }
        if (new_height > maxDimen) {
            new_height = maxDimen;
            new_width = (new_height * original_width) / original_height;
        }

        //Decode with inSampleSize
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(o, new_width, new_height);
//        Log.e("Sample size", "is " + options.inSampleSize);
//            options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(f.getAbsolutePath(), options);
    }

    @Nullable
    public Bitmap getBitmap() {
        if (TextUtils.isEmpty(path)) {
            return bitmap;
        } else {
            File imgFile = new File(path);
            if (imgFile.exists()) {
//                Log.e("Path", "abc" + path);
                if (context != null) {
                    return loadBitmapWithGlide(path);
                } else {
                    Bitmap bm;
                    bm = decodeFile(imgFile);
                    return rotateImage(bm, orientation);
                }
            }
        }
        return null;
    }

    private Bitmap loadBitmapWithGlide(String path) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(path).getAbsolutePath(), o);
//            Log.e("Test", "ABC");
            int original_width = o.outWidth;
            int original_height = o.outHeight;
            int new_width = original_width;
            int new_height = original_height;
            if (original_width > maxDimen) {
                new_width = maxDimen;
                new_height = (new_width * original_height) / original_width;
            }
            if (new_height > maxDimen) {
                new_height = maxDimen;
                new_width = (new_height * original_width) / original_height;
            }

            Bitmap bm = Glide.with(context).asBitmap().load(path).into(new_width, new_height).get();
            return rotateImage(bm, orientation);
        } catch (InterruptedException e) {
            logException(e);
        } catch (ExecutionException e) {
            logException(e);
        }
        return null;
    }

    public Uri getUri() {
        if (TextUtils.isEmpty(path)) {
            return null;
        } else {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                return Uri.fromFile(imgFile);
            }
        }
        return null;
    }

    @Nullable
    public Bitmap getScaledBitmap() {
        Bitmap origin_bm = getBitmap();
        if (origin_bm == null) return null;
        int original_width = origin_bm.getWidth();
        int original_height = origin_bm.getHeight();
        int new_width = original_width;
        int new_height = original_height;
        if (original_width > maxDimen) {
            new_width = maxDimen;
            new_height = (new_width * original_height) / original_width;
        }
        if (new_height > maxDimen) {
            new_height = maxDimen;
            new_width = (new_height * original_width) / original_height;
        }
        return BITMAP_RESIZER(origin_bm, new_width, new_height);
    }

    private Bitmap BITMAP_RESIZER(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setFilterBitmap(true);
        //canvas.drawBitmap(bitmap, matrix, paint);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, paint);
        return scaledBitmap;

    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        if (img == null) return null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        //        img.recycle();
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    public ImageEntry(final Builder builder) {
        this.path = builder.mPath;
        this.imageId = builder.mImageId;
        this.dateAdded = builder.mDateAdded;
    }

    public static ImageEntry from(final Cursor cursor) {
        return Builder.from(cursor).build();
    }

    public static ImageEntry from(final Uri uri) {
        return Builder.from(uri).build();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageEntry && ((ImageEntry) o).path.equals(path);
    }

    @NonNull
    @Override
    public String toString() {
        return "ImageEntry{" +
                "path='" + path + '\'' +
                '}';
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getCompressPercent() {
        return compressPercent;
    }

    public void setCompressPercent(int compressPercent) {
        this.compressPercent = compressPercent;
    }

    public static class Builder {

        public static int count = -1;
        private final String mPath;
        private int mImageId;
        private long mDateAdded;

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
            final int dateAddedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);

            final int imageId = cursor.getInt(imageIdColumn);
            final String path = cursor.getString(dataColumn);
            final long dateAdded = cursor.getLong(dateAddedColumn);

            return new Builder(path)
                    .imageId(imageId)
                    .dateAdded(dateAdded)
                    ;

        }


        public Builder imageId(int imageId) {
            this.mImageId = imageId;
            return this;
        }

        public Builder dateAdded(long timestamp) {
            this.mDateAdded = timestamp;
            return this;
        }


        public ImageEntry build() {
            return new ImageEntry(this);
        }


    }

    public int getMaxDimen() {
        return maxDimen;
    }

    public void setMaxDimen(int maxDimen) {
        this.maxDimen = maxDimen;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.imageId);
        dest.writeString(this.path);
        dest.writeLong(this.dateAdded);
        dest.writeByte(this.isPicked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isVideo ? (byte) 1 : (byte) 0);
        dest.writeInt(this.orientation);
        dest.writeInt(this.maxDimen);
        dest.writeInt(this.compressPercent);
        dest.writeString(this.description);
        dest.writeByte(this.uploaded ? (byte) 1 : (byte) 0);
        dest.writeInt(this.progress);
    }

    protected ImageEntry(Parcel in) {
        this.imageId = in.readInt();
        this.path = in.readString();
        this.dateAdded = in.readLong();
        this.isPicked = in.readByte() != 0;
        this.isVideo = in.readByte() != 0;
        this.orientation = in.readInt();
        this.maxDimen = in.readInt();
        this.compressPercent = in.readInt();
        this.description = in.readString();
        this.uploaded = in.readByte() != 0;
        this.progress = in.readInt();
    }

    public static final Creator<ImageEntry> CREATOR = new Creator<ImageEntry>() {
        @Override
        public ImageEntry createFromParcel(Parcel source) {
            return new ImageEntry(source);
        }

        @Override
        public ImageEntry[] newArray(int size) {
            return new ImageEntry[size];
        }
    };
}
