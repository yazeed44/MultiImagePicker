package net.yazeed44.imagepicker.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.yazeed44.imagepicker.util.AlbumEntry;
import net.yazeed44.imagepicker.util.ImageEntry;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by yazeed44 on 6/20/15.
 */
public class ImagePagerAdapter extends PagerAdapter {


    protected final AlbumEntry mAlbumEntry;
    protected final Context mContext;
    protected final PhotoViewAttacher.OnViewTapListener mTapListener;

    public ImagePagerAdapter(final AlbumEntry albumEntry, final Context context, final PhotoViewAttacher.OnViewTapListener tapListener) {
        mAlbumEntry = albumEntry;
        mContext = context;
        mTapListener = tapListener;
    }

    @Override
    public int getCount() {
        return mAlbumEntry.imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final ImageEntry imageEntry = mAlbumEntry.imageList.get(position);
        final PhotoView view = new PhotoView(mContext);
        view.setOnViewTapListener(mTapListener);


        Glide.with(mContext)
                .load(imageEntry.path)
                .into(view);

        container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
