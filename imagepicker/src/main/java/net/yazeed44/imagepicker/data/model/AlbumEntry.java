package net.yazeed44.imagepicker.data.model;

import net.yazeed44.imagepicker.util.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import java8.util.Objects;

/**
 * Created by yazeed44
 * on 6/14/15.
 */
public class AlbumEntry implements Serializable {
    public final int id;
    public final String name;
    public final ArrayList<ImageEntry> imageList = new ArrayList<>();
    public ImageEntry coverImage;


    public AlbumEntry(int albumId, String albumName) {
        this.id = albumId;
        this.name = albumName;
    }

    public void addPhoto(ImageEntry imageEntry) {
        imageList.add(imageEntry);
    }

    public void sortImagesByTimeDesc() {
        Collections.sort(imageList, (lhs, rhs) -> (int) (rhs.dateAdded - lhs.dateAdded));

        coverImage = imageList.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbumEntry)) return false;
        AlbumEntry that = (AlbumEntry) o;
        return id == that.id &&
                Objects.equals(Util.unAccent(name), Util.unAccent(that.name)) &&
                Objects.equals(imageList, that.imageList) &&
                Objects.equals(coverImage, that.coverImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, Util.unAccent(name), imageList, coverImage);
    }
}
