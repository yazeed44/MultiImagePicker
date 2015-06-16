package net.yazeed44.imagepicker.util;

import com.melnykov.fab.FloatingActionButton;

/**
 * Created by yazeed44 on 6/13/15.
 */
public final class Events {

    private Events() {

    }


    public static class OnClickAlbumEvent {
        public final AlbumEntry albumEntry;

        public OnClickAlbumEvent(final AlbumEntry albumEntry) {
            this.albumEntry = albumEntry;
        }
    }

    public static class OnPickImageEvent {
        public final ImageEntry imageEntry;

        public OnPickImageEvent(final ImageEntry imageEntry) {
            this.imageEntry = imageEntry;
        }
    }

    public static class OnUnpickImageEvent {
        public final ImageEntry imageEntry;

        public OnUnpickImageEvent(final ImageEntry imageEntry) {
            this.imageEntry = imageEntry;
        }
    }

    public static class OnAttachFabEvent {
        public final FloatingActionButton fab;

        public OnAttachFabEvent(FloatingActionButton fab) {
            this.fab = fab;
        }
    }

    public static class OnPublishPickOptions {
        public final Picker options;

        public OnPublishPickOptions(final Picker options) {
            this.options = options;
        }
    }
}
