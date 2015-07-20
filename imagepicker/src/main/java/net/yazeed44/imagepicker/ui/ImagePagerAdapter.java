package net.yazeed44.imagepicker.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.yazeed44.imagepicker.model.AlbumEntry;
import net.yazeed44.imagepicker.model.ImageEntry;


/**
 * Created by yazeed44 on 6/20/15.
 */
public class ImagePagerAdapter extends PagerAdapter {


    protected final AlbumEntry mAlbumEntry;
    protected final Context mContext;

    public ImagePagerAdapter(final Context context, final AlbumEntry albumEntry) {
        mAlbumEntry = albumEntry;
        mContext = context;
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
        final SubsamplingScaleImageView imageView = new SubsamplingScaleImageView(mContext);
        imageView.setImage(ImageSource.uri(imageEntry.path));

        container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


}
