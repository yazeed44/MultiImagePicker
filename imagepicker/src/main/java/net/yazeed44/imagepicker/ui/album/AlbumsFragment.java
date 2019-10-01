package net.yazeed44.imagepicker.ui.album;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.data.model.AlbumEntry;
import net.yazeed44.imagepicker.data.model.ImageEntry;
import net.yazeed44.imagepicker.data.Events;
import net.yazeed44.imagepicker.ui.PickerActivity;
import net.yazeed44.imagepicker.util.LoadingAlbumsRequest;
import net.yazeed44.imagepicker.util.OfflineSpiceService;
import net.yazeed44.imagepicker.util.Picker;
import net.yazeed44.imagepicker.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;


/**
 * Created by yazeed44
 * on 11/22/14.
 */
public class AlbumsFragment extends Fragment implements RequestListener<ArrayList<AlbumEntry>> {
    public static final String TAG = AlbumsFragment.class.getSimpleName();
    protected RecyclerView mAlbumsRecycler;
    protected SpiceManager mSpiceManager = new SpiceManager(OfflineSpiceService.class);
    protected ArrayList<AlbumEntry> mAlbumList;
    protected Picker mPickOptions;
    protected boolean mShouldPerformClickOnCapturedAlbum = false;

    public void setmPickOptions(Picker mPickOptions) {
        this.mPickOptions = mPickOptions;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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


    }

    @Override
    public void onRequestSuccess(final ArrayList<AlbumEntry> albumEntries) {

        if (hasLoadedSuccessfully(albumEntries)) {
            mAlbumList = albumEntries;

            final AlbumsAdapter albumsAdapter = new AlbumsAdapter(this, albumEntries, mAlbumsRecycler, mPickOptions);
            if (mAlbumsRecycler != null) mAlbumsRecycler.setAdapter(albumsAdapter);

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

    public void setupRecycler() {
        mAlbumsRecycler.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.num_columns_albums));
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);

        mAlbumsRecycler.setLayoutManager(gridLayoutManager);
    }

    public void setupAdapter() {
        if (mAlbumList == null) {
            final LoadingAlbumsRequest loadingRequest = new LoadingAlbumsRequest(getActivity(), mPickOptions);
            mSpiceManager.execute(loadingRequest, this);
        } else {
            mAlbumsRecycler.setAdapter(new AlbumsAdapter(this, mAlbumList, mAlbumsRecycler, mPickOptions));
        }
    }

    private boolean hasLoadedSuccessfully(final ArrayList albumList) {
        return albumList != null && albumList.size() > 0;
    }

    @Subscribe
    public void onEvent(final Events.OnReloadAlbumsEvent reloadAlbums) {
        mShouldPerformClickOnCapturedAlbum = true;

        EventBus.getDefault().removeStickyEvent(Events.OnAlbumsLoadedEvent.class);
        mAlbumList = null;
        setupAdapter();
    }

    private void pickLatestCapturedImage() {
        for (final AlbumEntry albumEntry : mAlbumList) {
            if (albumEntry.name.equals(PickerActivity.CAPTURED_IMAGES_ALBUM_NAME)) {
                AlbumEntry image = Util.getAllPhotosAlbum(mAlbumList);
                if (image != null) {
                    ImageEntry imageEntry = image.imageList.get(0);
                    EventBus.getDefault().postSticky(new Events.OnPickImageEvent(imageEntry));
                }
                if (mAlbumsRecycler.getChildAt(mAlbumList.indexOf(albumEntry)) != null)
                    mAlbumsRecycler.getChildAt(mAlbumList.indexOf(albumEntry)).performClick();
            }
        }
    }

}
