package net.yazeed44.imagepicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.ArrayList;


public class PickerActivity extends BaseActivity implements AlbumsFragment.OnClickAlbum , ImagesFragment.OnPickImage  {


    public static final String ALBUM_KEY = "albumKey";

    public static final String PICKED_IMAGES_KEY = "pickedImagesKey";

    public static final String LIMIT_KEY = "limitKey";

    public static final int NO_LIMIT = -1;
    private int limit = NO_LIMIT;
    public static final int PICK_REQUEST = 100;
    private ArrayList<AlbumUtil.PhotoEntry> checkedPhotos = new ArrayList<AlbumUtil.PhotoEntry>();
    private TextView doneBadge , doneText;
    private View doneLayout;
    private ImagesFragment imagesFragment;
    private AlbumsFragment albumsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);
        initImageLoader();

        doneBadge = (TextView) findViewById(R.id.done_button_badge);
        doneText = (TextView) findViewById(R.id.done_button_text);
        doneLayout = findViewById(R.id.done_btn);
        initOptions();
        updateTextAndBadge();



        setupAlbums(savedInstanceState);
    }


    private void initOptions(){

       final Intent options = getIntent();

        if (options != null) {

            try {
                limit = options.getExtras().getInt(LIMIT_KEY);
            } catch (NullPointerException ex) {
                limit = NO_LIMIT;
            }
        }

        AlbumUtil.initLimit(limit);

    }

    private void setupAlbums(Bundle savedInstanceState){
        if (findViewById(R.id.container) != null) {

            if (savedInstanceState == null) {



                albumsFragment = new AlbumsFragment();
                albumsFragment.setArguments(getIntent().getExtras());

                getSupportFragmentManager().beginTransaction()
                .add(R.id.container, albumsFragment)
                .commit();



            }
        }
    }


    private void initImageLoader(){


                String CACHE_DIR = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/.temp_tmp";
                new File(CACHE_DIR).mkdirs();
                File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(),
                        CACHE_DIR);
                DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                        .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                            .resetViewBeforeLoading(true)
                           .  build();
                ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                        getBaseContext())
                        .defaultDisplayImageOptions(defaultOptions)
                        .diskCache(new UnlimitedDiscCache(cacheDir))
                        .memoryCache(new WeakMemoryCache());
                ImageLoaderConfiguration config = builder.build();

                ImageLoader.getInstance().init(config);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.menu_camera) {
            //TODO
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void finish(){

        if (imagesFragment != null && imagesFragment.isVisible()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container,albumsFragment)
                    .commit();
        }

        else {
            super.finish();
        }
    }


    @Override
    public void onClickAlbum(AlbumUtil.AlbumEntry album) {
      final Bundle albumBundle = new Bundle();
        albumBundle.putSerializable(ALBUM_KEY,album);

         imagesFragment = new ImagesFragment();
        imagesFragment.setArguments(albumBundle);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, imagesFragment)
                .commit();
    }

    @Override
    public void onPickImage(AlbumUtil.PhotoEntry photoEntry) {
        if (limit == NO_LIMIT) {
            checkedPhotos.add(photoEntry);
        }

        else if (checkedPhotos.size() < limit){
            checkedPhotos.add(photoEntry);
        }




        updateTextAndBadge();
    }

    @Override
    public void onUnpickImage(AlbumUtil.PhotoEntry photo) {
        checkedPhotos.remove(photo);
        updateTextAndBadge();

    }


    private void updateTextAndBadge(){
        AlbumUtil.initCount(checkedPhotos.size());

        if (checkedPhotos.isEmpty()){
            doneBadge.setVisibility(View.GONE);
            doneLayout.setClickable(false);
            doneText.setTextColor(getResources().getColor(R.color.no_checked_photos_text));

        }

        else if (checkedPhotos.size() == limit){
            doneBadge.setText(checkedPhotos.size() + "");
            doneBadge.setBackgroundColor(getResources().getColor(R.color.reached_limit));
            Toast.makeText(this,R.string.reach_limit,Toast.LENGTH_SHORT).show();

        }

        else {
            doneText.setTextColor(Color.parseColor("#ffffff"));
            doneLayout.setClickable(true);
            doneBadge.setBackgroundColor(getResources().getColor(R.color.checked_photo));
            doneBadge.setVisibility(View.VISIBLE);
            doneBadge.setText(checkedPhotos.size() + "");
        }

    }

    public void onClickDone(View view){

        final String[] paths = new String[checkedPhotos.size()];

        for (int i = 0 ; i < checkedPhotos.size();i++){
            paths[i] = checkedPhotos.get(i).path;
        }

        final Intent data = new Intent().putExtra(PICKED_IMAGES_KEY,paths);
        setResult(RESULT_OK,data);
        super.finish();

    }

    public void onClickCancel(View view){
        setResult(RESULT_CANCELED);
        super.finish();

    }



}
