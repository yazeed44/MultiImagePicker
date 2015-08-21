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
import net.yazeed44.imagepicker.model.AlbumEntry;
import net.yazeed44.imagepicker.util.Events;
import net.yazeed44.imagepicker.util.LoadingAlbumsRequest;
import net.yazeed44.imagepicker.util.OfflineSpiceService;
import net.yazeed44.imagepicker.util.Picker;
import net.yazeed44.imagepicker.util.Util;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


/**
 * Created by yazeed44 on 11/22/14.
 */
public class AlbumsFragment extends Fragment implements RequestListener<ArrayList> {
    public static final String TAG = AlbumsFragment.class.getSimpleName();
    protected RecyclerView mAlbumsRecycler;
    protected SpiceManager mSpiceManager = new SpiceManager(OfflineSpiceService.class);
    protected ArrayList<AlbumEntry> mAlbumList;
    protected Picker mPickOptions;
    protected boolean mShouldPerformClickOnCapturedAlbum = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAlbumsRecycler = (RecyclerView) inflater.inflate(R.layout.fragment_album_browse, container, false);

        if (mPickOptions == null) {
            mPickOptions = EventBus.getDefault().getStickyEvent(Events.OnPublishPickOptionsEvent.class).options;
        }

        if (mAlbumList == null) {

            final Events.OnAlbumsLoadedEvent albumLoadedEvent = EventBus.getDefault().getStickyEvent(Events.OnAlbumsLoadedEvent.class);

            if (albumLoadedEvent != null) {
                mAlbumList = albumLoadedEvent.albumList;
            }
        }

        setupAdapter();
        setupRecycler();

        return mAlbumsRecycler;
    }

    @Override
    public void onStart() {
        mSpiceManager.start(getActivity());

        EventBus.getDefault().register(this);

        super.onStart();
    }

    @Override
    public void onStop() {
        if (mSpiceManager.isStarted()) {
            mSpiceManager.shouldStop();
        }
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        Log.e(TAG, spiceException.getMessage());

    }

    @Override
    public void onRequestSuccess(final ArrayList albumEntries) {

        if (hasLoadedSuccessfully(albumEntries)) {
            mAlbumList = albumEntries;

            final AlbumsAdapter albumsAdapter = new AlbumsAdapter(this, albumEntries, mAlbumsRecycler);
            mAlbumsRecycler.setAdapter(albumsAdapter);

            EventBus.getDefault().postSticky(new Events.OnAlbumsLoadedEvent(mAlbumList));


            if (!mShouldPerformClickOnCapturedAlbum) {
                return;
            }

            mAlbumsRecycler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (!mAlbumsRecycler.hasPendingAdapterUpdates()) {
                        pickLatestCapturedImage();
                        mShouldPerformClickOnCapturedAlbum = false;
                    } else {
                        mAlbumsRecycler.postDelayed(this, 100);
                    }

                }
            }, 100);
        }

    }

    protected void setupRecycler() {

        mAlbumsRecycler.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.num_columns_albums));
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mAlbumsRecycler.setLayoutManager(gridLayoutManager);


    }

    public void setupAdapter() {
        if (mAlbumList == null) {
            final LoadingAlbumsRequest loadingRequest = new LoadingAlbumsRequest(getActivity(), mPickOptions);

            mSpiceManager.execute(loadingRequest, this);
        } else {

            mAlbumsRecycler.setAdapter(new AlbumsAdapter(this, mAlbumList, mAlbumsRecycler));
        }
    }

    private boolean hasLoadedSuccessfully(final ArrayList albumList) {
        return albumList != null && albumList.size() > 0;
    }

    public void onEvent(final Events.OnReloadAlbumsEvent reloadAlbums) {
        mShouldPerformClickOnCapturedAlbum = true;

        EventBus.getDefault().removeStickyEvent(Events.OnAlbumsLoadedEvent.class);
        mAlbumList = null;
        setupAdapter();
    }

    private void pickLatestCapturedImage() {
            for (final AlbumEntry albumEntry : mAlbumList) {
                if (albumEntry.name.equals(PickerActivity.CAPTURED_IMAGES_ALBUM_NAME)) {
                    EventBus.getDefault().postSticky(new Events.OnPickImageEvent(Util.getAllPhotosAlbum(mAlbumList).imageList.get(0)));
                    mAlbumsRecycler.getChildAt(mAlbumList.indexOf(albumEntry)).performClick();
                }
            }


    }

}
