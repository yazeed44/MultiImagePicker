package net.yazeed44.imagepicker;

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


    public static int mLimit;
    public static int mCount;

    private AlbumUtil() {
        throw new AssertionError();
    }

    public static void initLimit(int limit) {
        mLimit = limit;
    }

    public static void initCount(int count) {
        mCount = count;
    }

    public static void loadAlbums(final GridView gridView, final AlbumsFragment fragment) {
        new Thread(new Runnable() {
            @Override
            public void run() {


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

                    cursor = MediaStore.Images.Media.query(gridView.getContext().getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            , projectionPhotos, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                    if (cursor != null) {
                        int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                        int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                        int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                        int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                        while (cursor.moveToNext()) {
                            int imageId = cursor.getInt(imageIdColumn);
                            int bucketId = cursor.getInt(bucketIdColumn);
                            String bucketName = cursor.getString(bucketNameColumn);
                            String path = cursor.getString(dataColumn);
                            long dateTaken = cursor.getLong(dateColumn);
                            int orientation = cursor.getInt(orientationColumn);
                            if (path == null || path.length() == 0) {
                                continue;
                            }
                            PhotoEntry photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation);
                            if (allPhotosAlbum == null) {
                                allPhotosAlbum = new AlbumEntry(0, gridView.getResources().getString(R.string.all_photos), photoEntry);
                                albumsSorted.add(0, allPhotosAlbum);
                            }
                            if (allPhotosAlbum != null) {
                                allPhotosAlbum.addPhoto(photoEntry);
                            }
                            AlbumEntry albumEntry = albums.get(bucketId);
                            if (albumEntry == null) {
                                albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry);
                                albums.put(bucketId, albumEntry);
                                if (cameraAlbumId == null && cameraFolder != null && path != null && path.startsWith(cameraFolder)) {
                                    albumsSorted.add(0, albumEntry);
                                    cameraAlbumId = bucketId;
                                } else {
                                    albumsSorted.add(albumEntry);
                                }
                            }
                            albumEntry.addPhoto(photoEntry);
                        }

                        setupAdapter(albumsSorted, gridView, fragment);
                    }
                } catch (Exception ex) {
                    Log.e("getAlbums", ex.getMessage());
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

            }
        }).start();


    }

    public static void setupAdapter(final ArrayList<AlbumEntry> albums, final GridView gridView, final AlbumsFragment fragment) {


        final AlbumsAdapter adapter = new AlbumsAdapter(albums, fragment);

        gridView.post(new Runnable() {
            @Override
            public void run() {
                gridView.setAdapter(adapter);
            }
        });

    }


    public static class AlbumEntry implements Serializable {
        public int bucketId;
        public String bucketName;
        public PhotoEntry coverPhoto;
        public ArrayList<PhotoEntry> photos = new ArrayList<PhotoEntry>();

        public AlbumEntry(int bucketId, String bucketName, PhotoEntry coverPhoto) {
            this.bucketId = bucketId;
            this.bucketName = bucketName;
            this.coverPhoto = coverPhoto;
        }

        public void addPhoto(PhotoEntry photoEntry) {
            photos.add(photoEntry);
        }
    }

    public static class PhotoEntry implements Serializable {
        public int bucketId;
        public int imageId;
        public long dateTaken;
        public String path;
        public int orientation;
        private boolean isPicked;

        public PhotoEntry(int bucketId, int imageId, long dateTaken, String path, int orientation) {
            this.bucketId = bucketId;
            this.imageId = imageId;
            this.dateTaken = dateTaken;
            this.path = path;
            this.orientation = orientation;
        }

        public boolean isPicked() {
            return isPicked;
        }

        public void setPicked(final boolean picked) {
            this.isPicked = picked;
        }

    }


}
