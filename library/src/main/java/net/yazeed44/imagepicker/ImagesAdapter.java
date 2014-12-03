package net.yazeed44.imagepicker;

import android.content.res.Resources;
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
class ImagesAdapter extends BaseAdapter {


    private final AlbumUtil.AlbumEntry mAlbum;
    private final ImagesFragment mFragment;

    public ImagesAdapter(final AlbumUtil.AlbumEntry album, final ImagesFragment fragment) {
        this.mAlbum = album;
        this.mFragment = fragment;
        setupItemListener();
    }


    @Override
    public int getCount() {
        return mAlbum.photos.size();
    }

    @Override
    public Object getItem(int position) {
        return mAlbum.photos.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AlbumUtil.PhotoEntry photo = mAlbum.photos.get(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = mFragment.getActivity().getLayoutInflater().inflate(R.layout.image, parent, false);
            holder = createHolder(convertView);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        setHeight(convertView);
        loadImage(holder, photo);
        drawGrid(convertView, holder, photo);


        return convertView;
    }

    private void setHeight(final View convertView) {


        final int height = (int) (mFragment.getResources().getDimensionPixelSize(R.dimen.image_width) * 1.1);

        convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

    }

    private void loadImage(final ViewHolder holder, final AlbumUtil.PhotoEntry photo) {

        ImageLoader.getInstance().displayImage("file://" + photo.path, holder.thumbnail);
    }

    private void drawGrid(final View convertView, final ViewHolder holder, final AlbumUtil.PhotoEntry photo) {

        final Resources r = mFragment.getResources();
        if (isPicked(photo)) {
            Log.d("drawGrid", photo.imageId + "   is picked");
            convertView.setBackgroundColor(r.getColor(R.color.checked_photo));
            holder.check.setBackgroundColor(r.getColor(R.color.checked_photo));
            final int padding = 10;
            holder.thumbnail.setPadding(padding, padding, padding, padding);
        } else {
            holder.check.setBackgroundColor(r.getColor(R.color.check_default_color));
            convertView.setBackgroundColor(r.getColor(android.R.color.transparent));
            holder.thumbnail.setPadding(0, 0, 0, 0);
        }
    }


    private void pickImage(final View convertView, final ViewHolder holder, final AlbumUtil.PhotoEntry photo) {

        final boolean isPicked = isPicked(photo);

        if (isPicked) {
            //Unpick

            mFragment.pickListener.onUnpickImage(photo);

        } else if (AlbumUtil.sLimit == PickerActivity.NO_LIMIT || AlbumUtil.sLimit > PickerActivity.sCheckedImages.size()) {
            //pick
            mFragment.pickListener.onPickImage(photo);

        }

        drawGrid(convertView, holder, photo);

    }

    private ViewHolder createHolder(final View child) {
        final ViewHolder holder = new ViewHolder();
        holder.thumbnail = (ImageView) child.findViewById(R.id.image_thumbnail);
        holder.check = (ImageView) child.findViewById(R.id.image_check);

        return holder;
    }


    private void setupItemListener() {
        mFragment.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AlbumUtil.PhotoEntry photoEntry = mAlbum.photos.get(position);
                final ViewHolder holder = createHolder(view);

                pickImage(view, holder, photoEntry);
            }
        });
    }

    private boolean isPicked(final AlbumUtil.PhotoEntry pPhotoEntry) {

        boolean isPicked = false;
        for (int i = 0; i < PickerActivity.sCheckedImages.size(); i++) {
            final AlbumUtil.PhotoEntry photo = PickerActivity.sCheckedImages.valueAt(i);

            if (photo.path.equals(pPhotoEntry.path)) {
                isPicked = true;
            }
        }

        return isPicked;
    }

    private static class ViewHolder {
        ImageView thumbnail;
        ImageView check;

    }
}
