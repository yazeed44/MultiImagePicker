package net.yazeed44.imagepicker;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.yazeed44.imagepicker.library.R;

/**
 * Created by yazeed44 on 11/23/14.
 */
public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder> implements Util.OnClickImage {


    private final Util.AlbumEntry mAlbum;
    private final RecyclerView mRecyclerView;

    public ImagesAdapter(final Util.AlbumEntry album, final RecyclerView fragment) {
        this.mAlbum = album;
        this.mRecyclerView = fragment;
    }

    @Override
    public ImagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View imageLayout = LayoutInflater.from(mRecyclerView.getContext()).inflate(R.layout.element_image, viewGroup, false);

        return new ImagesViewHolder(imageLayout, this);
    }

    @Override
    public int getItemCount() {
        return mAlbum.imageList.size();
    }

    @Override
    public void onBindViewHolder(ImagesViewHolder imagesViewHolder, int position) {
        final Util.ImageEntry photoEntry = mAlbum.imageList.get(position);
        setHeight(imagesViewHolder.itemView);
        displayThumbnail(imagesViewHolder, photoEntry);
        drawGrid(imagesViewHolder, photoEntry);

    }

    @Override
    public void onClickImage(View layout, ImageView thumbnail, ImageView check) {

        final int position = Util.getPositionOfChild(layout, R.id.image_layout, mRecyclerView);
        final ImagesViewHolder holder = (ImagesViewHolder) mRecyclerView.getChildViewHolder(layout);
        pickImage(holder, mAlbum.imageList.get(position));
    }


    public void setHeight(final View convertView) {


        final int height = mRecyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.image_height);

        convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

    }

    public void displayThumbnail(final ImagesViewHolder holder, final Util.ImageEntry photo) {


        Glide.with(mRecyclerView.getContext())
                .load(photo.path)
                .asBitmap()
                .centerCrop()
                .into(holder.thumbnail)
        ;


    }

    public void drawGrid(final ImagesViewHolder holder, final Util.ImageEntry imageEntry) {

        final Resources r = mRecyclerView.getContext().getResources();
        if (isPicked(imageEntry)) {
            holder.itemView.setBackgroundColor(r.getColor(R.color.checked_photo));
            holder.check.setBackgroundColor(r.getColor(R.color.checked_photo));


            holder.thumbnail.setColorFilter(mRecyclerView.getContext().getResources().getColor(R.color.checked_photo_overlay));
            final int padding = mRecyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.image_checked_padding);
            holder.itemView.setPadding(padding, padding, padding, padding);
        } else {
            holder.check.setBackgroundColor(r.getColor(R.color.check_default_color));
            holder.itemView.setBackgroundColor(r.getColor(android.R.color.transparent));
            holder.thumbnail.setColorFilter(Color.TRANSPARENT);
            holder.itemView.setPadding(0, 0, 0, 0);
        }
    }


    public void pickImage(final ImagesViewHolder holder, final Util.ImageEntry imageEntry) {

        final boolean isPicked = isPicked(imageEntry);

        if (isPicked) {
            //Unpick

            PickerActivity.BUS.post(new Events.OnUnpickImageEvent(imageEntry));


        } else if (Util.sLimit == PickerActivity.NO_LIMIT || Util.sLimit > PickerActivity.sCheckedImages.size()) {
            //pick
            PickerActivity.BUS.post(new Events.OnPickImageEvent(imageEntry));


        }

        drawGrid(holder, imageEntry);

    }


    public boolean isPicked(final Util.ImageEntry pImageEntry) {


        for (int i = 0; i < PickerActivity.sCheckedImages.size(); i++) {
            final Util.ImageEntry image = PickerActivity.sCheckedImages.valueAt(i);

            if (image.path.equals(pImageEntry.path)) {

                return true;
            }
        }

        return false;
    }


    class ImagesViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final ImageView check;

        public ImagesViewHolder(final View itemView, final Util.OnClickImage listener) {
            super(itemView);

            thumbnail = (ImageView) itemView.findViewById(R.id.image_thumbnail);
            check = (ImageView) itemView.findViewById(R.id.image_check);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickImage(itemView, thumbnail, check);
                }
            });

        }
    }


}
