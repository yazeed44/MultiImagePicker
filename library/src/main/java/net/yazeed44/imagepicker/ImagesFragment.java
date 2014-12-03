package net.yazeed44.imagepicker;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import net.yazeed44.imagepicker.library.R;


/**
 * Created by yazeed44 on 11/23/14.
 */
public class ImagesFragment extends Fragment {

    public GridView gridView;
    public OnPickImage pickListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        gridView = (GridView) inflater.inflate(R.layout.fragment_image_browse, container, false);

        setupAdapter();


        return gridView;
    }

    void setupAdapter() {
        final AlbumUtil.AlbumEntry album = (AlbumUtil.AlbumEntry) getArguments().getSerializable(PickerActivity.ALBUM_KEY);

        final ImagesAdapter adapter = new ImagesAdapter(album, this);


        gridView.setAdapter(adapter);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnPickImage) {
            pickListener = (OnPickImage) activity;


        } else {
            throw new ClassCastException(activity.toString() + "  Dosen't implement ImagesFragment.OnPickImage !!");
        }
    }


    public static interface OnPickImage {
        public void onPickImage(AlbumUtil.PhotoEntry photoEntry);

        public void onUnpickImage(AlbumUtil.PhotoEntry photo);
    }
}
