package net.yazeed44.imagepicker.util;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;

import net.yazeed44.imagepicker.data.model.AlbumEntry;

import java.util.ArrayList;

/**
 * Created by yazeed44
 * on 6/13/15.
 */
public class LoadingAlbumsRequest extends SpiceRequest<ArrayList<AlbumEntry>> {
    private final Context mContext;
    private final Picker mPickerOptions;

    @SuppressWarnings("unchecked")
    public LoadingAlbumsRequest(final Context context, final Picker pickerOptions) {
        super((Class) ArrayList.class);
        mContext = context;
        mPickerOptions = pickerOptions;
    }

    @Override
    public ArrayList<AlbumEntry> loadDataFromNetwork() {
        return Util.getAlbums(mContext, mPickerOptions);
    }
}
