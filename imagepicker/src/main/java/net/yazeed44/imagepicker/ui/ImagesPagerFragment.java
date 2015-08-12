package net.yazeed44.imagepicker.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.model.AlbumEntry;
import net.yazeed44.imagepicker.util.Events;

import de.greenrobot.event.EventBus;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by yazeed44 on 6/20/15.
 */
public class ImagesPagerFragment extends Fragment implements PhotoViewAttacher.OnViewTapListener, ViewPager.OnPageChangeListener {

    public static final String TAG = ImagesPagerFragment.class.getSimpleName();
    protected ViewPager mImagePager;
    protected AlbumEntry mSelectedAlbum;
    protected FloatingActionButton mDoneFab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        EventBus.getDefault().post(new Events.OnShowingToolbarEvent());
        removeBehaviorAttr(container);
        mImagePager = (ViewPager) inflater.inflate(R.layout.fragment_image_pager, container, false);


        mImagePager.addOnPageChangeListener(this);

        return mImagePager;
    }

    private void removeBehaviorAttr(final ViewGroup container) {
        //If the behavior hasn't been removed then when collapsing the toolbar the layout will resize which is annoying

        final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) container.getLayoutParams();
        layoutParams.setBehavior(null);
        container.setLayoutParams(layoutParams);
    }

    private void addBehaviorAttr(final ViewGroup container) {
        final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) container.getLayoutParams();
        layoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
        container.setLayoutParams(layoutParams);

    }

    @Override
    public void onDestroyView() {
        addBehaviorAttr((ViewGroup) mImagePager.getParent());
        super.onDestroyView();
        EventBus.getDefault().post(new Events.OnShowingToolbarEvent());

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);

        mDoneFab.hide();
    }

    @Override
    public void onViewTap(View view, float x, float y) {


        if (mDoneFab.isVisible()) {
            //Hide everything expect the image
            EventBus.getDefault().post(new Events.OnHidingToolbarEvent());
            mDoneFab.hide();


        } else {
            //Show fab and actionbar
            EventBus.getDefault().post(new Events.OnShowingToolbarEvent());
            mDoneFab.setVisibility(View.VISIBLE);
            mDoneFab.show();
            mDoneFab.bringToFront();

        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        updateDisplayedImage(position);

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateDisplayedImage(final int index) {
        EventBus.getDefault().post(new Events.OnChangingDisplayedImageEvent(mSelectedAlbum.imageList.get(index)));
        //Because index starts from 0
        final int realPosition = index + 1;
        final String actionbarTitle = getResources().getString(R.string.image_position_in_view_pager).replace("%", realPosition + "").replace("$", mSelectedAlbum.imageList.size() + "");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(actionbarTitle);

    }


    public void onEvent(final Events.OnPickImageEvent pickImageEvent) {

        mDoneFab.setVisibility(View.VISIBLE);
        mDoneFab.show();
        mDoneFab.bringToFront();
        if (mImagePager.getAdapter() != null) {
            return;
        }
        mImagePager.setAdapter(new ImagePagerAdapter(this, mSelectedAlbum, this));
        final int imagePosition = mSelectedAlbum.imageList.indexOf(pickImageEvent.imageEntry);

        mImagePager.setCurrentItem(imagePosition);

        updateDisplayedImage(imagePosition);


    }

    public void onEvent(final Events.OnClickAlbumEvent albumEvent) {
        mSelectedAlbum = albumEvent.albumEntry;
    }

    public void onEvent(final Events.OnAttachFabEvent fabEvent) {
        mDoneFab = fabEvent.fab;
    }


}
