package net.yazeed44.imagepicker.util;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.yazeed44.imagepicker.library.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yazeed44 on 11/22/14.
 */
public final class Util {




    private Util() {
        throw new AssertionError();
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


                    final ImageEntry imageEntry = ImageEntry.from(cursor);

                    if (imageEntry.path == null || imageEntry.path.length() == 0) {
                        continue;
                    }


                    if (allPhotosAlbum == null) {
                        allPhotosAlbum = new AlbumEntry(0, context.getResources().getString(R.string.all_photos), imageEntry);
                        albumsSorted.add(0, allPhotosAlbum);
                    }
                    if (allPhotosAlbum != null) {
                        allPhotosAlbum.addPhoto(imageEntry);
                    }
                    AlbumEntry albumEntry = albums.get(bucketId);
                    if (albumEntry == null) {
                        albumEntry = new AlbumEntry(bucketId, bucketName, imageEntry);
                        albums.put(bucketId, albumEntry);
                        if (cameraAlbumId == null && cameraFolder != null && imageEntry.path != null && imageEntry.path.startsWith(cameraFolder)) {
                            albumsSorted.add(0, albumEntry);
                            cameraAlbumId = bucketId;
                        } else {
                            albumsSorted.add(albumEntry);
                        }
                    }
                    albumEntry.addPhoto(imageEntry);
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



    public static int getPositionOfChild(final View child, final int childParentId, final RecyclerView recyclerView) {

        if (child.getId() == childParentId) {
            return recyclerView.getChildPosition(child);
        }


        View parent = (View) child.getParent();
        while (parent.getId() != childParentId) {
            parent = (View) parent.getParent();
        }
        return recyclerView.getChildPosition(parent);
    }

    public interface OnClickImage {
        void onClickImage(final View layout, final ImageView thumbnail, final ImageView check);
    }

    public interface OnClickAlbum {
        void onClickAlbum(final View layout);
    }


}
