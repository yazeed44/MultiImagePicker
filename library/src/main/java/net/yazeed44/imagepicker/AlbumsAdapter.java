package net.yazeed44.imagepicker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.yazeed44.imagepicker.library.R;

import java.util.ArrayList;

/**
 * Created by yazeed44 on 11/22/14.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder> implements Util.OnClickAlbum {

    protected final ArrayList<Util.AlbumEntry> mAlbumList;
    protected final RecyclerView mRecycler;

    public AlbumsAdapter(final ArrayList<Util.AlbumEntry> albums, RecyclerView mRecycler) {
        this.mAlbumList = albums;
        this.mRecycler = mRecycler;
    }


    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View layout = LayoutInflater.from(mRecycler.getContext()).inflate(R.layout.element_album, parent, false);
        return new AlbumViewHolder(layout, this);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {

        setHeight(holder.itemView);
        setupAlbum(holder, mAlbumList.get(position));

    }

    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }

    @Override
    public void onClickAlbum(View layout) {
        final int position = mRecycler.getChildPosition(layout);
        final Util.AlbumEntry album = mAlbumList.get(position);

        PickerActivity.BUS.post(new Events.OnClickAlbumEvent(album));


    }


    public void setHeight(final View layout) {

        final int height = layout.getResources().getDimensionPixelSize(R.dimen.album_height);

        layout.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));


    }

    public void setupAlbum(final AlbumViewHolder holder, final Util.AlbumEntry album) {
        holder.name.setText(album.name);
        holder.count.setText(album.imageList.size() + "");

        Glide.with(mRecycler.getContext())
                .load(album.coverImage.path)
                .asBitmap()
                .into(holder.thumbnail);
    }


    class AlbumViewHolder extends RecyclerView.ViewHolder {
        protected final ImageView thumbnail;
        protected final TextView count;
        protected final TextView name;


        public AlbumViewHolder(final View itemView, final Util.OnClickAlbum listener) {
            super(itemView);

            thumbnail = (ImageView) itemView.findViewById(R.id.album_thumbnail);
            count = (TextView) itemView.findViewById(R.id.album_count);
            name = (TextView) itemView.findViewById(R.id.album_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickAlbum(itemView);
                }
            });

        }
    }
}
