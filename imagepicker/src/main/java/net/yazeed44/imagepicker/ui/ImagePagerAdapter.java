package net.yazeed44.imagepicker.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.yazeed44.imagepicker.model.AlbumEntry;
import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Events;

import de.greenrobot.event.EventBus;


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
        setupTouchGesture(imageView, imageEntry);

        container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return imageView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private void setupTouchGesture(final SubsamplingScaleImageView imageView, final ImageEntry imageEntry) {
        final GestureDetector myGestureDetector = new GestureDetector(mContext, new MyTouchGesture());
        imageView.setImage(ImageSource.uri(imageEntry.path));
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return myGestureDetector.onTouchEvent(event);
            }
        });
    }

    private static class MyTouchGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            EventBus.getDefault().post(new Events.OnTapImageEvent());
            return super.onSingleTapConfirmed(e);
        }
    }
}
