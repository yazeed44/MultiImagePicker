package net.yazeed44.imagepicker.ui.imagePreview;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.yazeed44.imagepicker.data.model.ImageEntry;
import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.util.Picker;

import java.util.ArrayList;

/**
 * Created by yazeed44
 * on 11/23/14.
 */
public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder> {


    protected final ArrayList<ImageEntry> imageEntries;
    protected final RecyclerView mRecyclerView;
    protected final Picker mPickOptions;

    protected final Drawable mCheckIcon;
    protected final Drawable mVideoIcon;


    public ImagePreviewAdapter(final ArrayList<ImageEntry> imageEntries, final RecyclerView recyclerView, Picker pickOptions) {
        this.imageEntries = imageEntries;
        this.mRecyclerView = recyclerView;
        mPickOptions = pickOptions;

        mCheckIcon = createCheckIcon();
        mVideoIcon = createVideoIcon();
    }

    private Drawable createCheckIcon() {
        Drawable checkIcon = AppCompatResources.getDrawable(mRecyclerView.getContext(), R.drawable.ic_action_done_white);
        checkIcon = DrawableCompat.wrap(checkIcon);
        DrawableCompat.setTint(checkIcon, mPickOptions.checkIconTintColor);
        return checkIcon;
    }

    private Drawable createVideoIcon() {
        if (!mPickOptions.videosEnabled) {
            return null;
        }
        Drawable videoIcon = AppCompatResources.getDrawable(mRecyclerView.getContext(), R.drawable.ic_play_arrow);
        videoIcon = DrawableCompat.wrap(videoIcon);
        DrawableCompat.setTint(videoIcon, mPickOptions.videoIconTintColor);
        return videoIcon;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View imageLayout = LayoutInflater.from(mRecyclerView.getContext()).inflate(R.layout.element_image_preview, viewGroup, false);


        return new ImageViewHolder(imageLayout);
    }

    @Override
    public int getItemCount() {
        return imageEntries == null ? 0 : imageEntries.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int position) {
        imageViewHolder.description.setTag(position);
        final ImageEntry imageEntry = imageEntries.get(position);
        setHeight(imageViewHolder.itemView);
        displayThumbnail(imageViewHolder, imageEntry);
        drawGrid(imageViewHolder, imageEntry);
        imageViewHolder.description.setText(imageEntry.getDescription());

    }


    public void setHeight(final View convertView) {
        final int height = mRecyclerView.getMeasuredWidth() / mRecyclerView.getResources().getInteger(R.integer.num_columns_images);
        convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

    public void displayThumbnail(final ImageViewHolder holder, final ImageEntry photo) {
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .load(photo.path)
                .into(holder.thumbnail);
    }

    public void drawGrid(final ImageViewHolder holder, final ImageEntry imageEntry) {


        holder.videoIcon.setVisibility(View.GONE);
        holder.itemView.setBackgroundColor(mPickOptions.imageBackgroundColor);
        holder.thumbnail.setColorFilter(Color.TRANSPARENT);
        holder.itemView.setPadding(0, 0, 0, 0);


        if (imageEntry.isVideo) {
            holder.thumbnail.setColorFilter(mPickOptions.videoThumbnailOverlayColor, PorterDuff.Mode.MULTIPLY);
            holder.videoIcon.setImageDrawable(mVideoIcon);
            holder.videoIcon.setVisibility(View.VISIBLE);
        }

    }

    public ArrayList<ImageEntry> getImages() {
        return imageEntries;
    }


    class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final ImageView videoIcon;
        EditText description;

        public ImageViewHolder(final View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.edt_des);

            thumbnail = itemView.findViewById(R.id.image_thumbnail);
            videoIcon = itemView.findViewById(R.id.image_video_icon);

            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() < 0) return;
            });
            description.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    if (description.getTag() != null) {
                        int position = (int) description.getTag();
                        if (position >= 0 && position < imageEntries.size())
                            imageEntries.get(position).setDescription(s.toString());
                    }
                }
            });


        }
    }
}
