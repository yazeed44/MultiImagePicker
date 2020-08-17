package net.yazeed44.imagepicker.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.data.model.AlbumEntry;
import net.yazeed44.imagepicker.data.model.ImageEntry;
import net.yazeed44.imagepicker.ui.PickerActivity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Created by yazeed44
 * on 11/22/14.
 */
public final class Util {
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE

    };
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final TypedValue TYPED_VALUE = new TypedValue();
    public static final String CAMERA_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + "Camera/";

    private Util() {
        throw new AssertionError();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isStoragePermissionGranted(@Nullable Activity activity) {
        if (activity == null) return false;
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED || permission1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE
            );

            return false;
        } else return true;
    }


    public static ArrayList<AlbumEntry> getAlbums(final Context context, final Picker pickOptions) {
        final ArrayList<AlbumEntry> albumsSorted = new ArrayList<>();

        final SparseArray<AlbumEntry> albums = new SparseArray<>();
        AlbumEntry allPhotosAlbum;


        Cursor imagesCursor = null;
        Cursor videosCursor = null;
        try {

            imagesCursor = queryImages(context);

            allPhotosAlbum = iterateCursor(context, imagesCursor, null, albumsSorted, albums, false);
            if (pickOptions.videosEnabled) {

                videosCursor = queryVideos(context);
                iterateCursor(context, videosCursor, allPhotosAlbum, albumsSorted, albums, true);
            }

        } catch (Exception ex) {
//            Log.e("getAlbums", ex.getMessage());
        } finally {

            closeCursors(imagesCursor, videosCursor);

        }

        setPickedFlagForPickedImages(albumsSorted);

        for (final AlbumEntry album : albumsSorted) {
            album.sortImagesByTimeDesc();
        }
//

        Collections.sort(albumsSorted, new Comparator<AlbumEntry>() {
            @Override
            public int compare(AlbumEntry o1, AlbumEntry o2) {
                if (o1 == null || o2 == null) return 1;
                String a1Name = o1.name == null ? "" : unAccent(o1.name);
                String a2Name = o2.name == null ? "" : unAccent(o2.name);
                if (TextUtils.isEmpty(a1Name) || TextUtils.isEmpty(a2Name)) return 1;
                int o1Id = a1Name.contains("IMG") ? Integer.MAX_VALUE : o1.id;
                int o2Id = a2Name.contains("IMG") ? Integer.MAX_VALUE : o2.id;
                return Integer.compare(o1Id, o2Id);
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }
        });

        return albumsSorted;

    }

    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d").toLowerCase();
    }

    private static void closeCursors(final Cursor imagesCursor, final Cursor videosCursor) {

        if (imagesCursor != null) {
            imagesCursor.close();
        }
        if (videosCursor != null) {
            videosCursor.close();
        }
    }

    private static Cursor queryImages(final Context context) {

        final String[] projectionPhotos = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.ORIENTATION
        };

        return MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , projectionPhotos, "", null, MediaStore.Images.Media.DATE_MODIFIED + " DESC");
    }

    private static Cursor queryVideos(final Context context) {
        final String[] projectionVideos = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_MODIFIED
        };

        return MediaStore.Video.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                , projectionVideos);
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

    private static AlbumEntry iterateCursor(final Context context, final Cursor cursor, @Nullable AlbumEntry allPhotosAlbum, final ArrayList<AlbumEntry> albumsSorted, final SparseArray<AlbumEntry> albums, final boolean isVideoCursor) {

        if (cursor == null) return null;

        final int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);

        while (cursor.moveToNext()) {

            final ImageEntry imageEntry = getImageFromCursor(cursor, isVideoCursor);

            if (imageEntry.path == null || imageEntry.path.length() == 0) {
                continue;

            }

            allPhotosAlbum = createAllPhotosAlbumIfDoesntExist(context, allPhotosAlbum, albumsSorted);
            allPhotosAlbum.addPhoto(imageEntry);

            final int bucketId = cursor.getInt(bucketIdColumn);
            final String bucketName = cursor.getString(bucketNameColumn);
            AlbumEntry albumEntry = albums.get(bucketId);
            if (albumEntry == null) {
                albumEntry = createNewAlbumAndAddItToArray(albums, bucketId, bucketName);

                if (shouldCreateCameraAlbum(imageEntry)) {
                    addCameraAlbumToArray(albumsSorted, albumEntry);

                } else {
                    albumsSorted.add(albumEntry);
                }
            }


            albumEntry.addPhoto(imageEntry);
        }

        return allPhotosAlbum;


    }

    private static void addCameraAlbumToArray(final ArrayList<AlbumEntry> albumsSorted, final AlbumEntry albumEntry) {
        albumsSorted.add(0, albumEntry);
    }

    private static boolean shouldCreateCameraAlbum(final ImageEntry imageEntry) {
        return imageEntry.path.startsWith(CAMERA_FOLDER);
    }

    @NonNull
    private static AlbumEntry createNewAlbumAndAddItToArray(final SparseArray<AlbumEntry> albums, final int bucketId, final String bucketName) {

        final AlbumEntry albumEntry = new AlbumEntry(bucketId, bucketName);
        albums.put(bucketId, albumEntry);
        return albumEntry;
    }

    @NonNull
    public static ImageEntry getImageFromCursor(final Cursor cursor, final boolean isVideoCursor) {
        final ImageEntry imageEntry = ImageEntry.from(cursor);
        imageEntry.isVideo = isVideoCursor;
        return imageEntry;
    }

    @NonNull
    private static AlbumEntry createAllPhotosAlbumIfDoesntExist(Context context, AlbumEntry allPhotosAlbum, ArrayList<AlbumEntry> albumsSorted) {
        if (allPhotosAlbum == null) {
            allPhotosAlbum = new AlbumEntry(Integer.MIN_VALUE, context.getResources().getString(R.string.all_photos));
            addCameraAlbumToArray(albumsSorted, allPhotosAlbum);
        }
        return allPhotosAlbum;
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
