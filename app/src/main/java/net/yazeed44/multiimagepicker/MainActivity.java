package net.yazeed44.multiimagepicker;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import net.yazeed44.imagepicker.sample.R;
import net.yazeed44.imagepicker.util.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements Picker.PickListener {

    private static final String TAG = "Sample activity";
    private RecyclerView mImageSampleRecycler;
    private ArrayList<ImageEntry> mSelectedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageSampleRecycler = (RecyclerView) findViewById(R.id.images_sample);
        setupRecycler();


    }

    private void setupRecycler() {

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.num_columns_image_samples));
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mImageSampleRecycler.setLayoutManager(gridLayoutManager);


    }


    public void onClickPickImageSingle(View view) {

        new Picker.Builder(this, this)
                .setPickMode(Picker.PickMode.SINGLE_IMAGE)
                .build()
                .startActivity();
    }

    public void onClickPickImageMultipleWithLimit(View view) {
        new Picker.Builder(this, this)
                .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                .setLimit(6)
                .build()
                .startActivity();
    }

    public void onPickImageMultipleInfinite(View view) {
        new Picker.Builder(this, this)
                .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                .build()
                .startActivity();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();


        if (id == R.id.action_about) {
            showAbout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAbout() {

        final Spanned aboutBody = Html.fromHtml(getResources().getString(R.string.about_body_html));


        new MaterialDialog.Builder(this)
                .title(R.string.about_title)
                .content(aboutBody)
                .contentLineSpacing(1.6f)
                .show();


    }






    @Override
    public void onPickedSuccessfully(ArrayList<ImageEntry> images) {
        mSelectedImages = images;
        setupImageSamples();
        Log.d(TAG, "Picked images  " + images.toString());
    }

    private void setupImageSamples() {
        mImageSampleRecycler.setAdapter(new ImageSamplesAdapter());
    }

    @Override
    public void onCancel() {
        Log.i(TAG, "User canceled picker activity");

    }


    private class ImageSamplesAdapter extends RecyclerView.Adapter<ImageSampleViewHolder> {


        @Override
        public ImageSampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final ImageView imageView = new ImageView(parent.getContext());
            return new ImageSampleViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(ImageSampleViewHolder holder, int position) {

            final String path = mSelectedImages.get(position).path;
            loadImage(path, holder.thumbnail);
        }

        @Override
        public int getItemCount() {
            return mSelectedImages.size();
        }


        private void loadImage(final String path, final ImageView imageView) {
            imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 440));

            Glide.with(MainActivity.this)
                    .load(path)
                    .asBitmap()
                    .into(imageView);


        }


    }

    class ImageSampleViewHolder extends RecyclerView.ViewHolder {

        protected ImageView thumbnail;

        public ImageSampleViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView;
        }
    }
}
