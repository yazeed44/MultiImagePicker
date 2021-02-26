package net.yazeed44.imagepicker.data;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.yazeed44.imagepicker.data.model.AlbumEntry;
import net.yazeed44.imagepicker.data.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;

import java.util.ArrayList;

/**
 * Created by yazeed44
 * on 6/13/15.
 */
public final class Events {

    private Events() {

    }


    public final static class OnClickAlbumEvent {
        public final AlbumEntry albumEntry;

        public OnClickAlbumEvent(final AlbumEntry albumEntry) {
            this.albumEntry = albumEntry;
        }
    }

    public final static class OnPickImageEvent {
        public final ImageEntry imageEntry;

        public OnPickImageEvent(final ImageEntry imageEntry) {
            this.imageEntry = imageEntry;
        }
    }

    public final static class OnUnpickImageEvent {
        public final ImageEntry imageEntry;

        public OnUnpickImageEvent(final ImageEntry imageEntry) {
            this.imageEntry = imageEntry;
        }
    }

    public final static class OnAttachFabEvent {
        public final FloatingActionButton fab;

        public OnAttachFabEvent(FloatingActionButton fab) {
            this.fab = fab;
        }
    }

    public final static class OnPublishPickOptionsEvent {
        public final Picker options;

        public OnPublishPickOptionsEvent(final Picker options) {
            this.options = options;
        }
    }

    public final static class OnAlbumsLoadedEvent {
        public final ArrayList<AlbumEntry> albumList;

        public OnAlbumsLoadedEvent(final ArrayList<AlbumEntry> albumList) {
            this.albumList = albumList;
        }
    }

    public final static class OnChangingDisplayedImageEvent {
        public final ImageEntry currentImage;

        public OnChangingDisplayedImageEvent(ImageEntry currentImage) {

            this.currentImage = currentImage;
        }
    }

    public final static class OnUpdateImagesThumbnailEvent {

        public OnUpdateImagesThumbnailEvent() {

        }
    }

    public final static class OnShowingToolbarEvent {
    }

    public final static class OnHidingToolbarEvent {
    }

    public final static class OnReloadAlbumsEvent {

    }


    public static final class OnImagePreviewEvent {
        public final ArrayList<ImageEntry> imageEntries;

        public OnImagePreviewEvent(ArrayList<ImageEntry> sCheckedImages) {
            this.imageEntries = sCheckedImages;
        }
    }

    public static final class OnPublishPreview {
        public final ArrayList<ImageEntry> imageEntries;

        public OnPublishPreview(ArrayList<ImageEntry> imageEntries) {
            this.imageEntries = imageEntries;
        }
    }
}
