package net.yazeed44.imagepicker.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.util.AlbumEntry;
import net.yazeed44.imagepicker.util.Events;
import net.yazeed44.imagepicker.util.LoadingAlbumsRequest;
import net.yazeed44.imagepicker.util.OfflineSpiceService;
import net.yazeed44.imagepicker.util.Picker;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


/**
 * Created by yazeed44 on 11/22/14.
 */
public class AlbumsFragment extends Fragment implements RequestListener<ArrayList> {
    public static final String TAG = "Albums Fragment";
    protected RecyclerView mAlbumsRecycler;
    protected SpiceManager mSpiceManager = new SpiceManager(OfflineSpiceService.class);
    protected ArrayList<AlbumEntry> mAlbumList;
    protected Picker mPickOptions;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAlbumsRecycler = (RecyclerView) inflater.inflate(R.layout.fragment_album_browse, container, false);

        if (mPickOptions == null) {
            mPickOptions = EventBus.getDefault().getStickyEvent(Events.OnPublishPickOptionsEvent.class).options;
        }

        if (mAlbumList == null) {

            final Events.onAlbumsLoadedEvent albumLoadedEvent = EventBus.getDefault().getStickyEvent(Events.onAlbumsLoadedEvent.class);

            if (albumLoadedEvent != null) {
                mAlbumList = albumLoadedEvent.albumList;
            }


        }


        setupAdapter();



        setupRecycler();
        setupAdapter();

        return mAlbumsRecycler;
    }

    protected void setupRecycler() {

        mAlbumsRecycler.setHasFixedSize(true);


        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.num_columns_albums));
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mAlbumsRecycler.setLayoutManager(gridLayoutManager);


    }

    @Override
    public void onStart() {
        mSpiceManager.start(getActivity());
        super.onStart();
    }

    @Override
    public void onStop() {
        mSpiceManager.shouldStop();
        super.onStop();
    }


    public void setupAdapter() {
        if (mAlbumList == null) {
            final LoadingAlbumsRequest loadingRequest = new LoadingAlbumsRequest(getActivity());

            mSpiceManager.execute(loadingRequest, this);
        } else {

            mAlbumsRecycler.setAdapter(new AlbumsAdapter(mAlbumList, mAlbumsRecycler, mPickOptions));
        }



    }



    @Override
    public void onRequestFailure(SpiceException spiceException) {
        Log.e(TAG, spiceException.getMessage());

    }

    @Override
    public void onRequestSuccess(ArrayList albumEntries) {

        if (hasLoadedSuccessfully(albumEntries)) {
            mAlbumList = albumEntries;

            EventBus.getDefault().postSticky(new Events.onAlbumsLoadedEvent(mAlbumList));

            final AlbumsAdapter albumsAdapter = new AlbumsAdapter(albumEntries, mAlbumsRecycler, mPickOptions);
            mAlbumsRecycler.setAdapter(albumsAdapter);


        }

    }

    private boolean hasLoadedSuccessfully(final ArrayList albumList) {
        return albumList != null && albumList.size() > 0;
    }


   /* public void onEvent(final Events.OnAttachFabEvent fabEvent){
        fabEvent.fab.attachToRecyclerView(mAlbumsRecycler);
    }*/


}
