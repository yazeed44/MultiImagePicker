package net.yazeed44.imagepicker;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.yazeed44.library.R;

import java.util.ArrayList;

/**
 * Created by yazeed44 on 11/22/14.
 */
public class AlbumsAdapter extends BaseAdapter {

    private AlbumsFragment fragment;
    private final  ArrayList<AlbumUtil.AlbumEntry> albums;

    public AlbumsAdapter(final ArrayList<AlbumUtil.AlbumEntry> albums,final AlbumsFragment fragment){
        this.albums = albums;
        this.fragment = fragment;
    }


    @Override
    public int getCount() {
        return albums.size();
    }


    @Override
    public Object getItem(int position) {
        return albums.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AlbumUtil.AlbumEntry album = albums.get(position);
        View grid = convertView;

        final ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            grid = fragment.getActivity().getLayoutInflater().inflate(R.layout.album,parent,false);

            holder.thumbnail = (ImageView) grid.findViewById(R.id.album_thumbnail);
            holder.count = (TextView) grid.findViewById(R.id.album_count);
            holder.name = (TextView) grid.findViewById(R.id.album_name);
            grid.setTag(holder);
        }

        else {
            holder = (ViewHolder)grid.getTag();
        }

        setupGrid(grid, album);
        setupAlbum(holder,album);

        return grid;

    }
    private void setupGrid(final View grid , final AlbumUtil.AlbumEntry album){

        final int height = (int) (fragment.getResources().getDimensionPixelSize(R.dimen.album_width) * 1.5);

        grid.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));


        grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.listener.onClickAlbum(album);
            }
        });
    }

    private void setupAlbum(final ViewHolder holder,final AlbumUtil.AlbumEntry album){
      holder.name.setText(album.bucketName);
      holder.count.setText(album.photos.size() + "");

        ImageLoader.getInstance().displayImage("file://" + album.coverPhoto.path,holder.thumbnail);
    }

    private static class ViewHolder {
        ImageView thumbnail;
        TextView count;
        TextView name;

    }
}
