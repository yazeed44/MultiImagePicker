package net.yazeed44.imagepicker.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.model.AlbumEntry;
import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.ui.PickerActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yazeed44 on 11/22/14.
 */
public final class Util {


    public static final TypedValue TYPED_VALUE = new TypedValue();
    public static final String CAMERA_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + "Camera/";


    private Util() {
        throw new AssertionError();
    }

    public static ArrayList<AlbumEntry> getAlbums(final Context context, final Picker pickOptions) {

        final String[] projectionPhotos = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.ORIENTATION
        };

        final String[] projectionVideos = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_ADDED
        };


        final ArrayList<AlbumEntry> albumsSorted = new ArrayList<>();

        final HashMap<Integer, AlbumEntry> albums = new HashMap<Integer, AlbumEntry>();
        AlbumEntry allPhotosAlbum = null;
        Cursor cursor = null;
        Cursor videoCursor = null;

        try {

            cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos, "", null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            allPhotosAlbum = traverseCursor(context, cursor, allPhotosAlbum, albumsSorted, albums, false);
            if (pickOptions.videosEnabled) {
                videoCursor = MediaStore.Video.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        , projectionVideos);
                traverseCursor(context, videoCursor, allPhotosAlbum, albumsSorted, albums, true);
            }

        } catch (Exception ex) {
            Log.e("getAlbums", ex.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (videoCursor != null) {
                videoCursor.close();
            }
        }

        setPickedFlagForPickedImages(albumsSorted);

        for (final AlbumEntry album : albumsSorted) {
            album.sortImagesByTimeDesc();
        }



        return albumsSorted;

    }

    private static void setPickedFlagForPickedImages(final ArrayList<AlbumEntry> albumsSorted) {
        //Check all photos album
        final AlbumEntry allPhotosAlbum = getAllPhotosAlbum(albumsSorted);

        if (allPhotosAlbum == null) {
            return;
        }

        if (!PickerActivity.sCheckedImages.isEmpty() && !allPhotosAlbum.imageList.isEmpty()) {

            for (final ImageEntry checkedImage : PickerActivity.sCheckedImages) {
                for (final ImageEntry imageEntry : allPhotosAlbum.imageList) {
                    imageEntry.isPicked = imageEntry.equals(checkedImage);
                }
            }
        }
    }

    public static AlbumEntry getAllPhotosAlbum(final ArrayList<AlbumEntry> albumEntries) {
        for (final AlbumEntry albumEntry : albumEntries) {
            if (albumEntry.id == 0) {
                return albumEntry;
            }
        }
        return null;
    }

    private static AlbumEntry traverseCursor(final Context context, final Cursor cursor, AlbumEntry allPhotosAlbum, final ArrayList<AlbumEntry> albumsSorted, final HashMap<Integer, AlbumEntry> albums, final boolean isVideoCursor){
        if (cursor != null) {
            Integer cameraAlbumId = null;
            int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);

            while (cursor.moveToNext()) {
                int bucketId = cursor.getInt(bucketIdColumn);
                String bucketName = cursor.getString(bucketNameColumn);

                final ImageEntry imageEntry = ImageEntry.from(cursor);
                imageEntry.isVideo = isVideoCursor;

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
                    if (cameraAlbumId == null && CAMERA_FOLDER != null && imageEntry.path != null && imageEntry.path.startsWith(CAMERA_FOLDER)) {
                        albumsSorted.add(0, albumEntry);
                        cameraAlbumId = bucketId;
                    } else {
                        albumsSorted.add(albumEntry);
                    }
                }
                albumEntry.addPhoto(imageEntry);
            }

            return allPhotosAlbum;
        }

        return null;
    }

    public static int getPositionOfChild(final View child, final int childParentId, final RecyclerView recyclerView) {

        if (child.getId() == childParentId) {
            return recyclerView.getChildAdapterPosition(child);
        }


        View parent = (View) child.getParent();
        while (parent.getId() != childParentId) {
            parent = (View) parent.getParent();
        }
        return recyclerView.getChildAdapterPosition(parent);
    }

    public static int getActionBarHeight(final Context context) {
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, TYPED_VALUE, true);


        return TypedValue.complexToDimensionPixelSize(TYPED_VALUE.data, context.getResources().getDisplayMetrics());
    }

    public static int getActionBarThemeResId(final Context context) {
        context.getTheme().resolveAttribute(R.attr.actionBarTheme, TYPED_VALUE, true);

        return TYPED_VALUE.resourceId;
    }

    public static int getToolbarThemeResId(final Context context) {

        if (isActionbarThemeLight(context)) {
            return R.style.ThemeOverlay_AppCompat_ActionBar;

        } else {
            return R.style.ThemeOverlay_AppCompat_Dark_ActionBar;
        }

    }

    public static int getDefaultPopupTheme(final Context context) {

        if (isActionbarThemeLight(context)) {
            return R.style.ThemeOverlay_AppCompat_Dark;
        } else {
            return R.style.ThemeOverlay_AppCompat_Light;
        }

    }


    public static boolean isActionbarThemeLight(final Context context) {
        return context.getTheme().obtainStyledAttributes(getActionBarThemeResId(context), new int[]{R.attr.isLightTheme}).getBoolean(0, true);

    }

    public static int getDefaultIconTintColor(final Context context) {

        if (isActionbarThemeLight(context)) {
            return Color.BLACK;
        } else {
            return Color.WHITE;

        }
    }

    public interface OnClickImage {
        void onClickImage(final View layout, final ImageView thumbnail, final ImageView check);
    }

    public interface OnClickAlbum {
        void onClickAlbum(final View layout);
    }


}
