package io.github.jwharm.javagi.examples.mediastream;

import io.github.jwharm.javagi.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.gdk.Paintable;
import org.gnome.gdk.PaintableFlags;
import org.gnome.gdk.RGBA;
import org.gnome.gio.ApplicationFlags;
import org.gnome.glib.GLib;
import org.gnome.glib.Source;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.graphene.Rect;
import org.gnome.gtk.*;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

import static java.lang.Math.PI;

/**
 * This is a Java port of the Gtk Demo "Media Stream" example in the "Paintable" category.
 * It uses the Cairo Java bindings to draw the output (a spinning nuclear icon).
 */
public class Animation {

    /* Do a full rotation in 5 seconds.
     *
     * We do not save steps here but real timestamps.
     * GtkMediaStream uses microseconds, so we will do so, too.
     */
    private static final int DURATION = 5 * GLib.USEC_PER_SEC;

    // This is the function that draws the actual icon.
    private static void nuclearSnapshot(Snapshot snapshot, RGBA foreground, RGBA background,
                                double width, double height, double rotation) {

        final double RADIUS = 0.3;
        float w = (float) width;
        float h = (float) height;

        snapshot.appendColor(background,
                Rect.allocate().init(0, 0, w, h));

        float size = Math.min(w, h);

        snapshot.appendCairo(Rect.allocate().init((w - size) / 2.0f, (h - size) / 2.0f, size, size))
                .setSourceRGBA(foreground.readRed(), foreground.readGreen(), foreground.readBlue(), foreground.readAlpha())
                .translate(w / 2.0, h / 2.0)
                .scale(size, size)
                .rotate(rotation)

                .arc(0, 0, 0.1, -PI, PI)
                .fill()

                .setLineWidth(RADIUS)
                .setDash(new double[] {RADIUS * PI / 3}, 0.0)
                .arc(0, 0, RADIUS, -PI, PI)
                .stroke();
    }

    /**
     * The NuclearIcon class from the Simple Paintable example
     */
    public static class NuclearIcon extends GObject implements Paintable {

        public NuclearIcon(MemorySegment address) {
            super(address);
        }

        private static final Type gtype = Types.register(NuclearIcon.class);
        public static Type getType() {
            return gtype;
        }

        // We store the rotation value here.
        public double rotation;

        // Here, we implement the functionality required by the GdkPaintable interface

        @Override
        public void snapshot(org.gnome.gdk.Snapshot snapshot, double width, double height) {
            nuclearSnapshot((Snapshot) snapshot, RGBA.allocate(0, 0, 0, 1), // black
                    RGBA.allocate(0.9f, 0.75f, 0.15f, 1), // yellow
                    width, height,
                    rotation);
        }

        @Override
        public PaintableFlags getFlags() {
            // The flags are very useful to let GTK know that this image is never going to change.
            // This allows many optimizations and should therefore always be set.
            return PaintableFlags.CONTENTS.or(PaintableFlags.SIZE);
        }

        // Add a simple constructor
        public static NuclearIcon create(double rotation) {
            NuclearIcon nuclear = GObject.newInstance(NuclearIcon.getType());
            nuclear.rotation = rotation;
            return nuclear;
        }
    }

    /**
     * The NuclearMediaStream class from the MediaStream Paintable example
     */
    public static class NuclearMediaStream extends MediaStream implements Paintable {

        public NuclearMediaStream(MemorySegment address) {
            super(address);
        }

        private static final Type gtype = Types.register(Animation.NuclearMediaStream.class);
        public static Type getType() {
            return gtype;
        }

        // This variable stores the progress of our video.
        public long progress;

        /* This variable stores the timestamp of the last
         * time we updated the progress variable when the
         * video is currently playing.
         * This is so that we can always accurately compute the
         * progress we've had, even if the timeout does not
         * exactly work.
         */
        public long lastTime;


        // This variable holds the ID of the timer that
        // updates our progress variable.
        public int sourceId;

        // GtkMediaStream is a GdkPaintable. So when we want to display video,
        // we have to implement the interface.

        @Override
        public void snapshot(org.gnome.gdk.Snapshot snapshot, double width, double height) {
            nuclearSnapshot((Snapshot) snapshot, RGBA.allocate(0, 0, 0, 1), // black
                    RGBA.allocate(0.9f, 0.75f, 0.15f, 1), // yellow
                    width, height,
                    2 * PI * progress / DURATION);
        }

        @Override
        public Paintable getCurrentImage() {
            return NuclearIcon.create(2 * PI * progress / DURATION);
        }

        @Override
        public PaintableFlags getFlags() {
            return PaintableFlags.SIZE;
        }

        /**
         * Compute the time that has elapsed since the last time we were called
         * and add it to our current progress.
         */
        public boolean step() {
            long currentTime = Objects.requireNonNull(GLib.mainCurrentSource()).getTime();
            progress += currentTime - this.lastTime;

            // Check if we've ended
            if (progress > DURATION) {
                if (getLoop()) {
                    // We're looping. So make the progress loop using modulo
                    progress %= DURATION;
                } else {
                    // Just make sure we don't overflow
                    progress = DURATION;
                }
            }

            // Update the last time to the current timestamp.
            lastTime = currentTime;

            // Update the timestamp of the media stream
            update(progress);

            // We also need to invalidate our contents again.
            // After all, we are a video and not just an audio stream.
            invalidateContents();

            // Now check if we have finished playing and if so,
            // tell the media stream. The media stream will then
            // call our pause function to pause the stream.
            if (progress >= DURATION) {
                streamEnded();
            }

            // The timeout function is removed by the pause function,
            // so we can just always return this value.
            return GLib.SOURCE_CONTINUE;
        }

        @Override
        public boolean play() {
            // If we're already at the end of the stream, we don't want
            // to start playing and exit early.
            if (progress >= DURATION)
                return false;

            // We add the source only when we start playing.
            sourceId = GLib.timeoutAdd(10, this::step);

            // We also want to initialize our time, so that we can
            // do accurate updates.
            lastTime = GLib.getMonotonicTime();

            // We successfully started playing, so we return TRUE here.
            return true;
        }

        /**
         * This function will be called when a playing stream
         * gets paused.
         * So we remove the updating source here and set it
         * back to 0 so that the finalize function doesn't try
         * to remove it again.
         */
        @Override
        public void pause() {
            Source.remove(sourceId);
            sourceId = 0;
            lastTime = 0;
        }

        /**
         * This is optional functionality for media streams,
         * but not being able to seek is kinda boring.
         * And it's trivial to implement, so let's go for it.
         */
        @Override
        public void seek(long timestamp) {
            progress = timestamp;

            // Media streams are asynchronous, so seeking can take a while.
            // We however don't need that functionality, so we can just
            // report success.
            seekSuccess();

            // We also have to update our timestamp and tell the
            // paintable interface about the seek
            update(progress);
            invalidateContents();
        }

        // We need to implement the finalize function.
        @Override
        public void finalize_() {
            // We need to check if the source exists before
            // removing it as it only exists while we are playing.
            if (sourceId > 0)
                Source.remove(sourceId);

            // Don't forget to chain up to the parent class' implementation
            // of the finalize function.
            // super.finalize_();
        }

        /**
         * Media streams start paused, but they need to tell GTK once
         * they are initialized, so we do that here.
         */
        @InstanceInit
        public void init() {
            streamPrepared(false, true, true, DURATION);
        }

        // Add the simple constructor
        public static NuclearMediaStream create() {
            return GObject.newInstance(NuclearMediaStream.getType());
        }
    }

    public static void main(String[] args) {
        Application app = new Application("io.github.jwharm.javagi.examples.MediaStream", ApplicationFlags.DEFAULT_FLAGS);
        app.onActivate(() -> {
            ApplicationWindow window = ApplicationWindow.builder()
                    .application(app)
                    .title("Media Stream example")
                    .defaultWidth(300)
                    .defaultHeight(200)
                    .build();
            var nuclear = NuclearMediaStream.create();
            nuclear.setLoop(true);
            var video = Video.newForMediaStream(nuclear);
            window.setChild(video);
            window.present();
        });
        app.run(args);
    }
}
