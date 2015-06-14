package net.yazeed44.imagepicker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.yazeed44.imagepicker.library.R;


/**
 * Created by yazeed44 on 11/23/14.
 */
public class ImagesFragment extends Fragment {

    protected RecyclerView mImagesRecycler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mImagesRecycler = (RecyclerView) inflater.inflate(R.layout.fragment_image_browse, container, false);

        setupRecycler();


        return mImagesRecycler;
    }

    protected void setupRecycler() {

        mImagesRecycler.setHasFixedSize(true);
        mImagesRecycler.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.image_spacing)));

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.num_columns_images));
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mImagesRecycler.setLayoutManager(gridLayoutManager);
        mImagesRecycler.setAdapter(createAdapter());


    }

    protected ImagesAdapter createAdapter() {
        //TODO Replace getSerializable
        final Util.AlbumEntry album = (Util.AlbumEntry) getArguments().getSerializable(PickerActivity.ALBUM_KEY);

        return new ImagesAdapter(album, mImagesRecycler);

    }


}
