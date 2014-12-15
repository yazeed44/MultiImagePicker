package net.yazeed44.imagepicker;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.yazeed44.imagepicker.library.R;

/**
 * Created by yazeed44 on 11/23/14.
 */
public class ImagesAdapter extends BaseAdapter {


    public final AlbumUtil.AlbumEntry album;
    public final ImagesFragment fragment;

    public ImagesAdapter(final AlbumUtil.AlbumEntry album, final ImagesFragment fragment) {
        this.album = album;
        this.fragment = fragment;
        setupItemListener();
    }


    @Override
    public int getCount() {
        return album.photos.size();
    }

    @Override
    public Object getItem(int position) {
        return album.photos.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AlbumUtil.PhotoEntry photo = album.photos.get(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.image, parent, false);
            holder = createHolder(convertView);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        setHeight(convertView);
        displayThumbnail(holder, photo);
        drawGrid(convertView, holder, photo);


        return convertView;
    }

    public void setHeight(final View convertView) {


        final int height = fragment.getResources().getDimensionPixelSize(R.dimen.image_height);

        convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

    }

    public void displayThumbnail(final ViewHolder holder, final AlbumUtil.PhotoEntry photo) {

        ImageLoader.getInstance().displayImage("file://" + photo.path, holder.thumbnail);
    }

    public void drawGrid(final View convertView, final ViewHolder holder, final AlbumUtil.PhotoEntry photo) {

        final Resources r = fragment.getResources();
        if (isPicked(photo)) {
            Log.d("drawGrid", photo.imageId + "   is picked");
            convertView.setBackgroundColor(r.getColor(R.color.checked_photo));
            holder.check.setBackgroundColor(r.getColor(R.color.checked_photo));
            final int padding = fragment.getResources().getDimensionPixelSize(R.dimen.image_checked_padding);
            holder.thumbnail.setColorFilter(fragment.getResources().getColor(R.color.checked_photo_overlay));
            convertView.setPadding(padding, padding, padding, padding);
        } else {
            holder.check.setBackgroundColor(r.getColor(R.color.check_default_color));
            convertView.setBackgroundColor(r.getColor(android.R.color.transparent));
            holder.thumbnail.setColorFilter(Color.TRANSPARENT);
            convertView.setPadding(0, 0, 0, 0);
        }
    }


    public void pickImage(final View convertView, final ViewHolder holder, final AlbumUtil.PhotoEntry photo) {

        final boolean isPicked = isPicked(photo);

        if (isPicked) {
            //Unpick

            fragment.pickListener.onUnpickImage(photo);


        } else if (AlbumUtil.sLimit == PickerActivity.NO_LIMIT || AlbumUtil.sLimit > PickerActivity.sCheckedImages.size()) {
            //pick
            fragment.pickListener.onPickImage(photo);

        }

        drawGrid(convertView, holder, photo);

    }

    public ViewHolder createHolder(final View child) {
        final ViewHolder holder = new ViewHolder();
        holder.thumbnail = (ImageView) child.findViewById(R.id.image_thumbnail);
        holder.check = (ImageView) child.findViewById(R.id.image_check);

        return holder;
    }


    public void setupItemListener() {
        fragment.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AlbumUtil.PhotoEntry photoEntry = album.photos.get(position);
                final ViewHolder holder = createHolder(view);

                pickImage(view, holder, photoEntry);
            }
        });
    }

    public boolean isPicked(final AlbumUtil.PhotoEntry pPhotoEntry) {


        for (int i = 0; i < PickerActivity.sCheckedImages.size(); i++) {
            final AlbumUtil.PhotoEntry photo = PickerActivity.sCheckedImages.valueAt(i);

            if (photo.path.equals(pPhotoEntry.path)) {

                return true;
            }
        }

        return false;
    }


    public static class ViewHolder {
        ImageView thumbnail;
        ImageView check;

    }
}
