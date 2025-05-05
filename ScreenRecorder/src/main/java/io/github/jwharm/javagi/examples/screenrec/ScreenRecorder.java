package io.github.jwharm.javagi.examples.screenrec;

import io.github.jwharm.javagi.base.Out;
import io.github.jwharm.javagi.gobject.JavaClosure;
import org.freedesktop.gstreamer.base.BaseSink;
import org.freedesktop.gstreamer.gst.*;
import org.gnome.glib.GError;
import org.gnome.glib.GLib;
import org.gnome.glib.MainLoop;
import org.gnome.glib.Source;
import org.gnome.gobject.GObjects;

/**
 * Record the screen and save it to a file.
 * The recording will automatically stop after 5 seconds.
 * <p>
 * There is also an AppSink configured, that receives the stream data
 * and can perform custom actions on it.
 */
public class ScreenRecorder {

    private final static String FILENAME = "recording.ogg";
    private final static int TIMEOUT  = 5 * 1000; // milliseconds

    private final MainLoop loop;

    private boolean busCall(Bus bus, Message msg) {

        if (msg.readType().contains(MessageType.EOS)) {
            GLib.print("End of stream\n");
            loop.quit();
        }

        else if (msg.readType().contains(MessageType.ERROR)) {
            Out<GError> error = new Out<>();
            Out<String> debug = new Out<>();
            msg.parseError(error, debug);

            GLib.printerr("Error: %s\n", error.get().readMessage());

            loop.quit();
        }

        return true;
    }

    /**
     * This callback function is triggered when the appsink receives a data sample.
     */
    public void newSample(Element sink) {

        /*
         * GStreamer plugins aren't registered as introspectable types: In this specific
         * case, we need the GstApp-1.0.gir - but it isn't in the gir-files repository
         * currently. As a result, Java doesn't recognize the AppSink type, which is
         * derived from BaseSink (which is a known type), and as a fallback, it considers
         * the appsink to be an Element.
         * Trying to cast the Element to BaseSink will throw a ClassCastException, so
         * that is not an option. As a workaround, we manually construct a BaseSink from
         * the memory address of the Element.
         */
        var appsink = new BaseSink.BaseSinkImpl(sink.handle());

        // Get the buffer
        var sample = appsink.getLastSample();
        if (sample != null) {
            var buffer = sample.getBuffer();
            // We don't do anything with the data here - just print an asterisk for every sample
            GLib.print("*");
        }
    }

    public ScreenRecorder(String[] args) {

        // Initialisation
        Gst.init(new Out<>(args));

        loop = new MainLoop(null, false);

        // Create gstreamer elements
        Pipeline pipeline = new Pipeline("screen-recorder");
        Element source = ElementFactory.make("ximagesrc", "ximage-source");
        Element conv = ElementFactory.make("videoconvert", "video-converter");
        Element tee = ElementFactory.make("tee", "tee");
        Element queue1 = ElementFactory.make("queue", "queue1");
        Element appsink = ElementFactory.make("appsink", "appsink");
        Element queue2 = ElementFactory.make("queue", "queue2");
        Element encoder = ElementFactory.make("theoraenc", "theora-encoder");
        Element muxer = ElementFactory.make("oggmux", "ogg-muxer");
        Element filesink = ElementFactory.make("filesink", "file-sink");

        if (source == null || tee == null || queue1 == null || appsink == null || queue2 == null
                || muxer == null || encoder == null || conv == null || filesink == null) {
            GLib.printerr("One element could not be created. Exiting.\n");
            return;
        }

        // Set the output filename to the filesink element
        filesink.set("location", FILENAME, null);

        // Watch the message bus for messages
        Bus bus = pipeline.getBus();
        if (bus == null) {
            GLib.printerr("Cannot get the bus of the pipeline. Exiting.\n");
            return;
        }
        int busWatchId = bus.addWatch(0, this::busCall);

        // Let the appsink invoke the newSample() method (declare above)
        appsink.set("emit-signals", true, null);
        JavaClosure closure = new JavaClosure(() -> newSample(appsink));
        GObjects.signalConnectClosure(appsink, "new-sample", closure, false);

        // Add all elements into the pipeline
        pipeline.addMany(source, conv, tee, queue1, appsink, queue2, encoder, muxer, filesink, null);

        /* Link the pads:
         *                                                         / queue1 | appsink
         * ximagesrc | video/x-raw,framerate=5/1 | converter | tee
         *                                                         \ queue2 | encoder | muxer | file-output
         */
        source.linkFiltered(conv, Caps.simple("video/x-raw", "framerate", Fraction.getType(), 5, 1, null));
        conv.link(tee);
        tee.requestPadSimple("src_%u").link(queue1.getStaticPad("sink"));
        tee.requestPadSimple("src_%u").link(queue2.getStaticPad("sink"));
        queue1.link(appsink);
        queue2.linkMany(encoder, muxer, filesink, null);

        // Set the pipeline state
        GLib.print("Now recording to file: %s\n", FILENAME);
        pipeline.setState(State.PLAYING);

        // Stop after 5 seconds
        GLib.timeoutAddOnce(TIMEOUT, loop::quit);

        // Iterate
        GLib.print("Running...\n");
        loop.run();

        // Out of the main loop, clean up nicely
        GLib.print("Returned, stopping recording\n");
        pipeline.setState(State.NULL);
        Source.remove(busWatchId);
    }

    public static void main(String[] args) {
        new ScreenRecorder(args);
    }
}
