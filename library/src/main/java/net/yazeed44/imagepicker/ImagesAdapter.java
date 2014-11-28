package net.yazeed44.imagepicker;

import android.content.res.Resources;
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
public class ImagesAdapter extends BaseAdapter {


    private final AlbumUtil.AlbumEntry album;
    private final ImagesFragment fragment;
    public ImagesAdapter(final AlbumUtil.AlbumEntry album , final ImagesFragment fragment){
        this.album = album;
        this.fragment = fragment;
        setupItemListener();
    }


    @Override
    public int getCount() {
        return album.photos.size();
    }

    @Override
    public Object getItem(int position) {
        return album.photos.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AlbumUtil.PhotoEntry photo = album.photos.get(position);

        final ViewHolder holder;
        if (convertView == null){
            convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.image,parent,false);
            holder = createHolder(convertView);



            convertView.setTag(holder);
        }

        else {
          holder = (ViewHolder)convertView.getTag();

        }

        setHeight(convertView);
        loadImage(holder,photo);
        drawGrid(convertView,holder,photo);



        return convertView;
    }


    private void drawGrid(final View convertView , final ViewHolder holder , final AlbumUtil.PhotoEntry photo){
        final Resources r = fragment.getResources();
        if (photo.isPicked()){
           convertView.setBackgroundResource(R.drawable.image_border);
            convertView.setBackgroundColor(fragment.getResources().getColor(R.color.checked_photo));
            holder.check.setBackgroundColor(fragment.getResources().getColor(R.color.checked_photo));
        }

        else {
            holder.check.setBackgroundColor(r.getColor(R.color.check_default_color));
            convertView.setBackgroundColor(r.getColor(android.R.color.transparent));
        }
    }

    private void setHeight(final View convertView) {


        final int height = (int) (fragment.getResources().getDimensionPixelSize(R.dimen.image_width) * 1.1);

        convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));




    }


    private void pickImage(final View convertView,final ViewHolder holder, final AlbumUtil.PhotoEntry photo){

        if (photo.isPicked()){
            //Unpick
            album.photos.get(album.photos.indexOf(photo)).setPicked(false);
           drawGrid(convertView,holder,photo);
            fragment.pickListener.onUnpickImage(photo);
        }

        else if (AlbumUtil.mLimit > AlbumUtil.mCount) {
            //pick
            album.photos.get(album.photos.indexOf(photo)).setPicked(true);
            drawGrid(convertView,holder,photo);
            fragment.pickListener.onPickImage(photo);
        }

    }

    private ViewHolder createHolder(final View child){
        final ViewHolder holder = new ViewHolder();
        holder.thumbnail = (ImageView) child.findViewById(R.id.image_thumbnail);
        holder.check = (ImageView) child.findViewById(R.id.image_check);

        return holder;
    }


    private void loadImage(final ViewHolder holder , final AlbumUtil.PhotoEntry photo){

        ImageLoader.getInstance().displayImage("file://" + photo.path ,holder.thumbnail);
    }

    private void setupItemListener() {
        fragment.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AlbumUtil.PhotoEntry photo = album.photos.get(position);
                final ViewHolder holder = createHolder(view);
                pickImage(view, holder, photo);
            }
        });
    }

    private static class ViewHolder{
        ImageView thumbnail;
        ImageView check;

    }
}
