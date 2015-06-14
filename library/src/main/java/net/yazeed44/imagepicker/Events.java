package net.yazeed44.imagepicker;

/**
 * Created by yazeed44 on 6/13/15.
 */
public final class Events {

    private Events() {

    }


    public static class OnClickAlbumEvent {
        public final Util.AlbumEntry albumEntry;

        public OnClickAlbumEvent(final Util.AlbumEntry albumEntry) {
            this.albumEntry = albumEntry;
        }
    }

    public static class OnPickImageEvent {
        public final Util.ImageEntry imageEntry;

        public OnPickImageEvent(final Util.ImageEntry imageEntry) {
            this.imageEntry = imageEntry;
        }
    }

    public static class OnUnpickImageEvent {
        public final Util.ImageEntry imageEntry;

        public OnUnpickImageEvent(final Util.ImageEntry imageEntry) {
            this.imageEntry = imageEntry;
        }
    }
}
