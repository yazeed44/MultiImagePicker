package net.yazeed44.imagepicker.ui.imagePreview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.yazeed44.imagepicker.data.Events;
import net.yazeed44.imagepicker.data.model.ImageEntry;
import net.yazeed44.imagepicker.library.R;
import net.yazeed44.imagepicker.ui.SpacesItemDecoration;
import net.yazeed44.imagepicker.util.LocaleHelper;
import net.yazeed44.imagepicker.util.Picker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class ImagePreviewActivity extends AppCompatActivity {
    public static final int REQUEST_PREVIEW = 1120;
    //        @Inject
//    PhotoPresenter<PhotoView> presenter;
    public static Picker mPickOptions;
    Toolbar toolbar;
    RecyclerView rvImages;
    FloatingActionButton mDoneFab;
    private ArrayList<ImageEntry> imageEntries = new ArrayList<>();
    ImagePreviewAdapter imagePreviewAdapter;
    private String descriptionHint;

    @Subscribe(sticky = true)
    public void onEvent(Events.OnPublishPickOptionsEvent event) {
        if (mPickOptions == null)
            mPickOptions = event.options;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(sticky = true)
    public void onEvent(Events.OnImagePreviewEvent event) {
        imageEntries = event.imageEntries;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);

//        getActivityComponent().inject(this);
        setContentView(R.layout.activity_album_pick);
//        setStatusBarColor(R.color.white);
//        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);
//        StatusBarUtil.setLightMode(this);
        toolbar = findViewById(R.id.album_toolbar);
        rvImages = findViewById(R.id.image_recycler);
        mDoneFab = findViewById(R.id.fab_done);
        toolbar.setTitle(R.string.add_description);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Drawable doneIcon = AppCompatResources.getDrawable(this, R.drawable.ic_action_done_white);
        if (doneIcon != null) {
            doneIcon = DrawableCompat.wrap(doneIcon);
            DrawableCompat.setTint(doneIcon, mPickOptions.doneFabIconTintColor);
        }
        mDoneFab = findViewById(R.id.fab_done);
        mDoneFab.setImageDrawable(doneIcon);
        mDoneFab.setBackgroundTintList(ColorStateList.valueOf(mPickOptions.fabBackgroundColor));
        mDoneFab.setRippleColor(mPickOptions.fabBackgroundColorWhenPressed);
        mDoneFab.setOnClickListener(view -> onDoneClicked());

//        presenter.onAttach(this);
        rvImages.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        if (getIntent().getExtras() != null) {
            descriptionHint = getIntent().getExtras().getString("descriptionHint");
        }
        rvImages.setLayoutManager(gridLayoutManager);
//        if (rvImages.getItemDecorationCount() <= 0)
//            rvImages.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.image_spacing)));
        imagePreviewAdapter = new ImagePreviewAdapter(imageEntries, rvImages, mPickOptions, descriptionHint);
        rvImages.setAdapter(imagePreviewAdapter);


    }

    boolean done = false;

    void onDoneClicked() {
        done = true;
        EventBus.getDefault().post(new Events.OnPublishPreview(imagePreviewAdapter.getImages()));
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (!done) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }
}
