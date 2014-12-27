package net.yazeed44.imagepicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import net.yazeed44.imagepicker.library.R;

import java.io.File;


public class PickerActivity extends ActionBarActivity implements AlbumsFragment.OnClickAlbum, ImagesFragment.OnPickImage {


    public static final String ALBUM_KEY = "albumKey";

    public static final String PICKED_IMAGES_KEY = "pickedImagesKey";
    public static final String LIMIT_KEY = "limitKey";

    public static final int PICK_REQUEST = 144;
    public static final int NO_LIMIT = -1;

    private int mLimit = NO_LIMIT;

    public static SparseArray<AlbumUtil.PhotoEntry> sCheckedImages = new SparseArray<AlbumUtil.PhotoEntry>();
    private TextView mDoneBadge, mDoneText;
    private View mDoneLayout;
    private ImagesFragment mImagesFragment;
    private AlbumsFragment mAlbumsFragment;
    private Uri mCapturedPhotoUri;


    //TODO Add animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_pick);
        getSupportActionBar().setTitle(R.string.albums_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        initImageLoader();


        initOptions();
        mDoneBadge = (TextView) findViewById(R.id.done_button_badge);
        mDoneText = (TextView) findViewById(R.id.done_button_text);
        mDoneLayout = findViewById(R.id.done_btn);
        updateTextAndBadge();


        setupAlbums(savedInstanceState);
    }


    public void initOptions() {


        sCheckedImages.clear();
        final Intent options = getIntent();

        if (options != null) {

            try {
                mLimit = options.getExtras().getInt(LIMIT_KEY);
            } catch (NullPointerException ex) {
                mLimit = NO_LIMIT;
            }


        }


        AlbumUtil.initLimit(mLimit);

    }


    public void setupAlbums(Bundle savedInstanceState) {
        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState == null) {


                mAlbumsFragment = new AlbumsFragment();
                mAlbumsFragment.setArguments(getIntent().getExtras());

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mAlbumsFragment)
                        .commit();


            }
        }
    }


    private void initImageLoader() {


        String CACHE_DIR = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/.temp_tmp";
        new File(CACHE_DIR).mkdirs();
        File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(),
                CACHE_DIR);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
                .build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                getBaseContext())
                .defaultDisplayImageOptions(defaultOptions)
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .memoryCache(new WeakMemoryCache());
        ImageLoaderConfiguration config = builder.build();

        ImageLoader.getInstance().init(config);


    }


    public void updateTextAndBadge() {

        if (sCheckedImages.size() == 0) {
            mDoneBadge.setVisibility(View.GONE);
            mDoneLayout.setClickable(false);
            mDoneText.setTextColor(getResources().getColor(R.color.no_checked_photos_text));

        } else if (sCheckedImages.size() == mLimit) {
            mDoneBadge.setText(sCheckedImages.size() + "");
            mDoneBadge.getBackground().setColorFilter(getResources().getColor(R.color.reached_limit_text), PorterDuff.Mode.SRC);
            Toast.makeText(this, R.string.reach_limit, Toast.LENGTH_SHORT).show();

        } else {
            mDoneText.setTextColor(Color.parseColor("#ffffff"));
            mDoneLayout.setClickable(true);
            mDoneBadge.getBackground().setColorFilter(getResources().getColor(R.color.checked_photo), PorterDuff.Mode.SRC);
            mDoneBadge.setVisibility(View.VISIBLE);
            mDoneBadge.setText(sCheckedImages.size() + "");
        }

    }

    public void onClickDone(View view) {

        final String[] paths = new String[sCheckedImages.size()];

        for (int i = 0; i < sCheckedImages.size(); i++) {
            paths[i] = sCheckedImages.valueAt(i).path;
        }

        final Intent data = new Intent().putExtra(PICKED_IMAGES_KEY, paths);
        setResult(RESULT_OK, data);
        super.finish();

    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        super.finish();


    }

    public void capturePhoto() {

        final File captureFolder = new File(Environment.getExternalStorageDirectory(), "capture" + System.currentTimeMillis() + ".png");

        captureFolder.mkdirs();

        mCapturedPhotoUri = Uri.fromFile(captureFolder);

        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedPhotoUri);
        startActivityForResult(captureIntent, 0);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == 0 && data == null) {

            sCheckedImages.append((int) System.currentTimeMillis(), new AlbumUtil.PhotoEntry.Builder(mCapturedPhotoUri.getPath()).build());
            updateTextAndBadge();

        } else {
            Log.i("onActivityResult", "User canceled the camera activity");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_take_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_take_photo) {
            capturePhoto();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void finish() {

        if (mImagesFragment != null && mImagesFragment.isVisible()) {
            mAlbumsFragment = new AlbumsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mAlbumsFragment)
                    .commit();
            getSupportActionBar().setTitle(R.string.albums_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            super.finish();
        }
    }


    @Override
    public void onClickAlbum(AlbumUtil.AlbumEntry album) {
        final Bundle albumBundle = new Bundle();
        albumBundle.putSerializable(ALBUM_KEY, album);

        mImagesFragment = new ImagesFragment();
        mImagesFragment.setArguments(albumBundle);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mImagesFragment)
                .commit();

        getSupportActionBar().setTitle(album.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onPickImage(AlbumUtil.PhotoEntry photoEntry) {
        if (mLimit == NO_LIMIT || sCheckedImages.size() < mLimit) {
            sCheckedImages.put(photoEntry.imageId, photoEntry);
        } else {
            Log.i("onPickImage", "You can't check more images");
        }


        updateTextAndBadge();
    }

    @Override
    public void onUnpickImage(AlbumUtil.PhotoEntry photo) {
        sCheckedImages.remove(photo.imageId);

        updateTextAndBadge();
    }


    @Override
    public boolean onSupportNavigateUp() {
        if (mImagesFragment != null && mImagesFragment.isVisible()) {
            finish();
            return true;
        }

        return false;
    }


}
