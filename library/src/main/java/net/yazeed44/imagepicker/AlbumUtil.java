package net.yazeed44.imagepicker;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridView;

import net.yazeed44.imagepicker.library.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yazeed44 on 11/22/14.
 */
public final class AlbumUtil {


    public static int sLimit;


    private AlbumUtil() {
        throw new AssertionError();
    }

    public static void initLimit(int limit) {
        sLimit = limit;
    }


    public static void loadAlbums(final GridView gridView, final AlbumsFragment fragment) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                final ArrayList<AlbumEntry> albums = getAlbums(gridView.getContext());
                setupAdapter(albums, gridView, fragment);

            }
        }).start();

    }

    public static ArrayList<AlbumEntry> getAlbums(final Context context) {


        final String[] projectionPhotos = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.ORIENTATION
        };


        final ArrayList<AlbumEntry> albumsSorted = new ArrayList<AlbumEntry>();

        HashMap<Integer, AlbumEntry> albums = new HashMap<Integer, AlbumEntry>();
        AlbumEntry allPhotosAlbum = null;
        String cameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + "Camera/";
        Integer cameraAlbumId = null;
        Cursor cursor = null;

        try {

            cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            if (cursor != null) {

                int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);

                while (cursor.moveToNext()) {
                    int bucketId = cursor.getInt(bucketIdColumn);
                    String bucketName = cursor.getString(bucketNameColumn);


                    PhotoEntry photoEntry = createPhoto(cursor);

                    if (photoEntry.path == null || photoEntry.path.length() == 0) {
                        continue;
                    }


                    if (allPhotosAlbum == null) {
                        allPhotosAlbum = new AlbumEntry(0, context.getResources().getString(R.string.all_photos), photoEntry);
                        albumsSorted.add(0, allPhotosAlbum);
                    }
                    if (allPhotosAlbum != null) {
                        allPhotosAlbum.addPhoto(photoEntry);
                    }
                    AlbumEntry albumEntry = albums.get(bucketId);
                    if (albumEntry == null) {
                        albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry);
                        albums.put(bucketId, albumEntry);
                        if (cameraAlbumId == null && cameraFolder != null && photoEntry.path != null && photoEntry.path.startsWith(cameraFolder)) {
                            albumsSorted.add(0, albumEntry);
                            cameraAlbumId = bucketId;
                        } else {
                            albumsSorted.add(albumEntry);
                        }
                    }
                    albumEntry.addPhoto(photoEntry);
                }


            }
        } catch (Exception ex) {
            Log.e("getAlbums", ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        return albumsSorted;

    }

    private static PhotoEntry createPhoto(final Cursor cursor) {
        final int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        final int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
        final int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
        final int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);

        int imageId = cursor.getInt(imageIdColumn);
        int bucketId = cursor.getInt(bucketIdColumn);
        String path = cursor.getString(dataColumn);
        long dateTaken = cursor.getLong(dateColumn);
        int orientation = cursor.getInt(orientationColumn);

        return new PhotoEntry.Builder(path)
                .albumId(bucketId)
                .dateTaken(dateTaken)
                .orientation(orientation)
                .imageId(imageId)
                .build();

    }

    private static void setupAdapter(final ArrayList<AlbumEntry> albums, final GridView gridView, final AlbumsFragment fragment) {


        final AlbumsAdapter adapter = new AlbumsAdapter(albums, fragment);

        gridView.post(new Runnable() {
            @Override
            public void run() {
                gridView.setAdapter(adapter);
            }
        });

    }


    public static class AlbumEntry implements Serializable {
        public final int Id;
        public final String name;
        public final PhotoEntry coverPhoto;
        public final ArrayList<PhotoEntry> photos = new ArrayList<PhotoEntry>();

        public AlbumEntry(int albumId, String albumName, PhotoEntry coverPhoto) {
            this.Id = albumId;
            this.name = albumName;
            this.coverPhoto = coverPhoto;
        }

        public void addPhoto(PhotoEntry photoEntry) {
            photos.add(photoEntry);
        }
    }

    public static class PhotoEntry implements Serializable {
        public final int albumId;
        public final int imageId;
        public final long dateTaken;
        public final String path;
        public final int orientation;

        public PhotoEntry(final Builder builder) {
            this.albumId = builder.mAlbumId;
            this.path = builder.mPath;
            this.orientation = builder.mOrientation;
            this.imageId = builder.mImageId;
            this.dateTaken = builder.mDateTaken;
        }


        public static class Builder {

            private final String mPath;
            private int mAlbumId;
            private int mImageId;
            private long mDateTaken;
            private int mOrientation;

            public Builder(final String path) {
                this.mPath = path;
            }

            public Builder albumId(int albumId) {
                this.mAlbumId = albumId;
                return this;
            }

            public Builder imageId(int imageId) {
                this.mImageId = imageId;
                return this;
            }

            public Builder dateTaken(final long dateTaken) {
                this.mDateTaken = dateTaken;
                return this;
            }

            public Builder orientation(final int orientation) {
                this.mOrientation = orientation;
                return this;
            }

            public PhotoEntry build() {
                return new PhotoEntry(this);
            }


        }

    }


}
