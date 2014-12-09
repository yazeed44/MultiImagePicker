package net.yazeed44.imagepicker;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.yazeed44.imagepicker.library.R;

import java.util.ArrayList;

/**
 * Created by yazeed44 on 11/22/14.
 */
public class AlbumsAdapter extends BaseAdapter {

    public final ArrayList<AlbumUtil.AlbumEntry> albums;
    public final AlbumsFragment fragment;

    public AlbumsAdapter(final ArrayList<AlbumUtil.AlbumEntry> albums, final AlbumsFragment fragment) {
        this.albums = albums;
        this.fragment = fragment;
        setupItemListener();
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
        if (convertView == null) {

            grid = fragment.getActivity().getLayoutInflater().inflate(R.layout.album, parent, false);
            holder = createHolder(grid);
            grid.setTag(holder);
        } else {
            holder = (ViewHolder) grid.getTag();
        }

        setHeight(grid);
        setupAlbum(holder, album);

        return grid;

    }

    public void setHeight(final View grid) {

        final int height = (int) (fragment.getResources().getDimensionPixelSize(R.dimen.album_width) * 1.5);

        grid.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));


    }

    public void setupAlbum(final ViewHolder holder, final AlbumUtil.AlbumEntry album) {
        holder.name.setText(album.name);
        holder.count.setText(album.photos.size() + "");

        ImageLoader.getInstance().displayImage("file://" + album.coverPhoto.path, holder.thumbnail);
    }

    public ViewHolder createHolder(View view) {
        final ViewHolder holder = new ViewHolder();
        holder.name = (TextView) view.findViewById(R.id.album_name);
        holder.count = (TextView) view.findViewById(R.id.album_count);
        holder.thumbnail = (ImageView) view.findViewById(R.id.album_thumbnail);
        return holder;
    }

    public void setupItemListener() {
        fragment.albumsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                final AlbumUtil.AlbumEntry album = albums.get(position);
                fragment.listener.onClickAlbum(album);
            }
        });
    }

    public static class ViewHolder {
        ImageView thumbnail;
        TextView count;
        TextView name;

    }
}
