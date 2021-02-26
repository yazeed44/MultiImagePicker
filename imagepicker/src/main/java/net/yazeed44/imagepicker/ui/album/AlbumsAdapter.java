package net.yazeed44.imagepicker.ui.album;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.data.model.AlbumEntry;
import net.yazeed44.imagepicker.data.Events;
import net.yazeed44.imagepicker.util.Picker;
import net.yazeed44.imagepicker.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by yazeed44
 * on 11/22/14.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder> implements Util.OnClickAlbum {

    private final RecyclerView mRecycler;
    private final ArrayList<AlbumEntry> mAlbumList;
    private final Picker mPickOptions;
    private final Fragment mFragment;

    public AlbumsAdapter(final Fragment fragment, final ArrayList<AlbumEntry> albums, final RecyclerView recyclerView, Picker mPickOptions) {
        mFragment = fragment;
        this.mAlbumList = albums;
        this.mPickOptions = mPickOptions;
        this.mRecycler = recyclerView;
//        Events.OnPublishPickOptionsEvent event = EventBus.getDefault().getStickyEvent(Events.OnPublishPickOptionsEvent.class);
//        mPickOptions = event.options;
    }


    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View layout = LayoutInflater.from(mRecycler.getContext()).inflate(R.layout.element_album, parent, false);

        return new AlbumViewHolder(layout, this);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        final AlbumEntry albumEntry = mAlbumList.get(position);
        setHeight(holder.itemView);
        setupAlbum(holder, albumEntry);

    }

    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }

    @Override
    public void onClickAlbum(View layout) {
        // Log.d(getClass().getSimpleName(), "onClickAlbum() called with: layout = [" + layout + "]");
        final int position = mRecycler.getChildAdapterPosition(layout);
        final AlbumEntry album = mAlbumList.get(position);

        EventBus.getDefault().postSticky(new Events.OnClickAlbumEvent(album));


    }


    public void setHeight(final View layout) {

        final int height = mRecycler.getMeasuredWidth() / mRecycler.getResources().getInteger(R.integer.num_columns_albums);

        layout.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));


    }

    public void setupAlbum(final AlbumViewHolder holder, final AlbumEntry album) {

        holder.name.setTextColor(mPickOptions.albumNameTextColor);
        holder.count.setTextColor(mPickOptions.albumImagesCountTextColor);


        holder.name.setText(album.name);
        holder.count.setText(String.valueOf(album.imageList.size()));

        holder.detailsLayout.setBackgroundColor(mPickOptions.albumBackgroundColor);

        Glide.with(mFragment)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .load(album.coverImage.path)
                .into(holder.thumbnail);

    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        protected final ImageView thumbnail;
        protected final TextView count;
        protected final TextView name;
        protected final RelativeLayout detailsLayout;


        public AlbumViewHolder(final View itemView, final Util.OnClickAlbum listener) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.album_thumbnail);
            count = itemView.findViewById(R.id.album_count);
            name = itemView.findViewById(R.id.album_name);
            detailsLayout = itemView.findViewById(R.id.album_detail_layout);

            itemView.setOnClickListener(v -> {
                if (getAbsoluteAdapterPosition() < 0) return;
                listener.onClickAlbum(itemView);
            });

        }
    }
}
