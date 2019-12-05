package net.yazeed44.imagepicker.ui.photoPager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.github.chrisbanes.photoview.OnViewTapListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.data.model.AlbumEntry;
import net.yazeed44.imagepicker.data.Events;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * Created by yazeed44
 * on 6/20/15.
 */
public class ImagesPagerFragment extends Fragment implements OnViewTapListener, ViewPager.OnPageChangeListener {

    public static final String TAG = ImagesPagerFragment.class.getSimpleName();
    protected ViewPager mImagePager;
    protected AlbumEntry mSelectedAlbum;
    protected FloatingActionButton mDoneFab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);

        if (mDoneFab != null) mDoneFab.hide();
    }

    @Override
    public void onViewTap(View view, float x, float y) {


        if (mDoneFab.isShown()) {
            //Hide everything expect the image
            EventBus.getDefault().post(new Events.OnHidingToolbarEvent());
            mDoneFab.hide();


        } else {
            //Show fab and actionbar
            EventBus.getDefault().post(new Events.OnShowingToolbarEvent());
//            mDoneFab.setVisibility(View.VISIBLE);
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
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (ab != null) {
                ab.setTitle(actionbarTitle);
            }
        }
    }


    @Subscribe(sticky = true)
    public void onEvent(final Events.OnPickImageEvent pickImageEvent) {

//        mDoneFab.setVisibility(View.VISIBLE);
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

    @Subscribe(sticky = true)
    public void onEvent(final Events.OnClickAlbumEvent albumEvent) {
        mSelectedAlbum = albumEvent.albumEntry;
    }

    @Subscribe(sticky = true)
    public void onEvent(final Events.OnAttachFabEvent fabEvent) {
        mDoneFab = fabEvent.fab;
    }


}
