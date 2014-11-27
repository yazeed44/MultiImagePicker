package net.yazeed44.imagepicker;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import net.yazeed44.library.R;


/**
 * Created by yazeed44 on 11/22/14.
 */
public class AlbumsFragment extends Fragment {

    public OnClickAlbum listener;



    private GridView mAlbums;

    public static final String TAG = "AlbumsFragmentTag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAlbums = (GridView)inflater.inflate(R.layout.fragment_album_browse, container, false);



        setupAdapter();

        return mAlbums;
    }

    public void setupAdapter(){
       net.yazeed44.imagepicker.AlbumUtil.loadAlbums(mAlbums, this);

    }








    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if (activity instanceof OnClickAlbum){
            listener = (OnClickAlbum) activity;
        }
        else {
         throw new ClassCastException(activity.toString() + "  Dosen't implement OnClickAlbum !!");
        }
    }






    public static interface OnClickAlbum {

        void onClickAlbum(net.yazeed44.imagepicker.AlbumUtil.AlbumEntry album);
    }
}
