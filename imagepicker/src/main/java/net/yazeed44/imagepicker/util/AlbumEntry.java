package net.yazeed44.imagepicker.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by yazeed44 on 6/14/15.
 */
public class AlbumEntry implements Serializable {
    public final int id;
    public final String name;
    public final ImageEntry coverImage;
    public final ArrayList<ImageEntry> imageList = new ArrayList<>();

    public AlbumEntry(int albumId, String albumName, ImageEntry coverImage) {
        this.id = albumId;
        this.name = albumName;
        this.coverImage = coverImage;
    }

    public void addPhoto(ImageEntry photoEntry) {
        imageList.add(photoEntry);
    }
}
