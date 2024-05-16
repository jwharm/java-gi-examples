package io.github.jwharm.javagi.examples.logging;

import org.gnome.glib.GLib;

import static org.gnome.glib.LogLevelFlags.*;

/**
 * Register a custom GLogWriterFunc that uses SLF4J for logging.
 */
public class Logging {

    public static void main(String[] args) {
        GLib.logSetWriterFunc(new SLF4JLogWriterFunc());

        GLib.log("logging-example", LEVEL_MESSAGE, "Hello %s\n", "world");
        GLib.log("logging-example", LEVEL_MESSAGE, "%d + %d = %d\n", 1, 1, 2);
        GLib.log("logging-example", LEVEL_WARNING, "This is a warning\n");
    }
}
