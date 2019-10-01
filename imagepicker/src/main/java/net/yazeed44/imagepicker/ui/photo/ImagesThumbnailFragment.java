package net.yazeed44.imagepicker.ui.photo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.data.Events;
import net.yazeed44.imagepicker.ui.SpacesItemDecoration;
import net.yazeed44.imagepicker.util.Picker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * Created by yazeed44
 * on 11/23/14.
 */
public class ImagesThumbnailFragment extends Fragment {

    public static final String TAG = ImagesThumbnailFragment.class.getSimpleName();
    protected RecyclerView mImagesRecycler;
    protected Picker mPickOptions;

    public void setmPickOptions(Picker mPickOptions) {
        this.mPickOptions = mPickOptions;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mImagesRecycler = (RecyclerView) inflater.inflate(R.layout.fragment_image_browse, container, false);

        setupRecycler();

//        mPickOptions = EventBus.getDefault().getStickyEvent(Events.OnPublishPickOptionsEvent.class).options;

        return mImagesRecycler;
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void setupRecycler() {

        mImagesRecycler.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.num_columns_images));

        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mImagesRecycler.setLayoutManager(gridLayoutManager);
        if (mImagesRecycler.getItemDecorationCount() <= 0)
            mImagesRecycler.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.image_spacing)));


    }

    @Subscribe(sticky = true)
    public void onEvent(final Events.OnClickAlbumEvent event) {
        mImagesRecycler.setAdapter(new ImagesThumbnailAdapter(this, event.albumEntry, mImagesRecycler, mPickOptions));
    }

    @Subscribe(sticky = true)
    public void onEvent(final Events.OnAttachFabEvent fabEvent) {
//        fabEvent.fab.attachToRecyclerView(mImagesRecycler);
    }

    @Subscribe
    public void onEvent(final Events.OnUpdateImagesThumbnailEvent redrawImage) {
        if (mImagesRecycler.getAdapter() != null)
            mImagesRecycler.getAdapter().notifyDataSetChanged();

    }


}
