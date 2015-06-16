package net.yazeed44.imagepicker.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.util.AlbumEntry;
import net.yazeed44.imagepicker.util.Events;
import net.yazeed44.imagepicker.util.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;

import java.io.File;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class PickerActivity extends AppCompatActivity {



    public static final int NO_LIMIT = -1;
    public static ArrayList<ImageEntry> sCheckedImages = new ArrayList<>();
    private ImagesFragment mImagesFragment;
    private AlbumsFragment mAlbumsFragment;
    private Uri mCapturedPhotoUri;

    private com.melnykov.fab.FloatingActionButton mDoneFab;
    private Picker mPickOptions;
    private ArrayList<AlbumEntry> mAlbumList;


    //TODO Add animation
    //TODO Fix bugs with changing orientation
    //TODO Add support for gif
    //TODO Use robust method for capturing photos

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_pick);
        getSupportActionBar().setTitle(R.string.albums_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        mDoneFab = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.fab_done);

        EventBus.getDefault().postSticky(new Events.OnAttachFabEvent(mDoneFab));

        mPickOptions = (EventBus.getDefault().getStickyEvent(Events.OnPublishPickOptionsEvent.class)).options;

        getTheme().setTo(mPickOptions.context.getTheme());

        initOptions();
        updateFab();


        setupAlbums(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void initOptions() {


        mDoneFab.setImageDrawable(getResources().getDrawable(mPickOptions.doneIconResId));
        mDoneFab.setColorNormal(mPickOptions.fabBackgroundColor);
        mDoneFab.setColorPressed(mPickOptions.fabBackgroundColorWhenPressed);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPickOptions.actionBarBackgroundColor));


    }


    public void setupAlbums(Bundle savedInstanceState) {
        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState == null) {

                mAlbumsFragment = new AlbumsFragment();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mAlbumsFragment)
                        .commit();


            }
        }
    }


    public void updateFab() {

        if (sCheckedImages.size() == 0) {
            mDoneFab.setVisibility(View.GONE);

        } else if (sCheckedImages.size() == mPickOptions.limit) {

            mDoneFab.setVisibility(View.VISIBLE);
            mDoneFab.show();

        } else {
            mDoneFab.setVisibility(View.VISIBLE);
            mDoneFab.show();


        }

    }

    public void onClickDone(View view) {

        final String[] paths = new String[sCheckedImages.size()];


        for (int i = 0; i < paths.length; i++) {
            paths[i] = sCheckedImages.get(i).path;
        }


        super.finish();
        mPickOptions.pickListener.onPickedSuccessfully(paths);
        sCheckedImages.clear();

    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        super.finish();
        mPickOptions.pickListener.onCancel();
        sCheckedImages.clear();


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
            //For capturing image from camera

            sCheckedImages.add(ImageEntry.from(mCapturedPhotoUri));
            updateFab();

        } else {
            Log.i("onActivityResult", "User canceled the camera activity");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_take_photo, menu);

        menu.findItem(R.id.action_take_photo).setIcon(mPickOptions.captureIconResId);


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
    public void onBackPressed() {

        if (mImagesFragment != null && mImagesFragment.isVisible()) {
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setTitle(R.string.albums_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();

        return true;
    }


    public void onEvent(final Events.OnClickAlbumEvent albumEvent) {


        if (mImagesFragment == null) {
            mImagesFragment = new ImagesFragment();
        }



        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mImagesFragment)
                .addToBackStack(null)
                .commit();

        getSupportActionBar().setTitle(albumEvent.albumEntry.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public void onEvent(final Events.OnPickImageEvent pickImageEvent) {
        if (mPickOptions.limit == NO_LIMIT || sCheckedImages.size() < mPickOptions.limit) {
            sCheckedImages.add(pickImageEvent.imageEntry);
        } else {
            Toast.makeText(this, R.string.you_cant_check_more_images, Toast.LENGTH_SHORT).show();
            Log.i("onPickImage", "You can't check more images");

        }


        updateFab();
    }


    public void onEvent(final Events.OnUnpickImageEvent unpickImageEvent) {
        sCheckedImages.remove(unpickImageEvent.imageEntry);

        updateFab();
    }

    public void onEvent(final Events.onAlbumsLoadedEvent albumsLoadedEvent) {
        mAlbumList = albumsLoadedEvent.albumList;
    }


}
