package net.yazeed44.imagepicker.ui;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.model.AlbumEntry;
import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Events;
import net.yazeed44.imagepicker.util.Picker;
import net.yazeed44.imagepicker.util.Util;

import de.greenrobot.event.EventBus;

/**
 * Created by yazeed44 on 11/23/14.
 */
public class ImagesThumbnailAdapter extends RecyclerView.Adapter<ImagesThumbnailAdapter.ImagesViewHolder> implements Util.OnClickImage {


    protected final AlbumEntry mAlbum;
    protected final RecyclerView mRecyclerView;
    protected final Picker mPickOptions;


    public ImagesThumbnailAdapter(final AlbumEntry album, final RecyclerView fragment, Picker pickOptions) {
        this.mAlbum = album;
        this.mRecyclerView = fragment;
        mPickOptions = pickOptions;
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
        final ImageEntry photoEntry = mAlbum.imageList.get(position);
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

    public void displayThumbnail(final ImagesViewHolder holder, final ImageEntry photo) {


        Glide.with(mRecyclerView.getContext())
                .load(photo.path)
                .asBitmap()
                .centerCrop()
                .into(holder.thumbnail)
        ;


    }

    public void drawGrid(final ImagesViewHolder holder, final ImageEntry imageEntry) {


        if (isPicked(imageEntry)) {
            holder.itemView.setBackgroundColor(mPickOptions.imageBackgroundColorWhenChecked);
            holder.check.setBackgroundColor(mPickOptions.imageBackgroundColorWhenChecked);


            holder.thumbnail.setColorFilter(mPickOptions.checkedImageOverlayColor);
            final int padding = mRecyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.image_checked_padding);
            holder.itemView.setPadding(padding, padding, padding, padding);
        } else {

            holder.check.setBackgroundColor(mPickOptions.imageCheckColor);
            holder.itemView.setBackgroundColor(mPickOptions.imageBackgroundColor);
            holder.thumbnail.setColorFilter(Color.TRANSPARENT);
            holder.itemView.setPadding(0, 0, 0, 0);
        }

        if (mPickOptions.pickMode == Picker.PickMode.SINGLE_IMAGE) {
            holder.check.setVisibility(View.GONE);
        }
    }


    public void pickImage(final ImagesViewHolder holder, final ImageEntry imageEntry) {

        final boolean isPicked = isPicked(imageEntry);

        if (isPicked) {
            //Unpick

            EventBus.getDefault().post(new Events.OnUnpickImageEvent(imageEntry));


        } else {
            //pick
            EventBus.getDefault().postSticky(new Events.OnPickImageEvent(imageEntry));


        }

        drawGrid(holder, imageEntry);

    }


    public boolean isPicked(final ImageEntry pImageEntry) {


        for (final ImageEntry imageEntry : PickerActivity.sCheckedImages) {

            if (imageEntry.equals(pImageEntry)) {
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
