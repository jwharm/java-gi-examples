package io.github.jwharm.javagi.examples.notepad;

import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.gio.ApplicationFlags;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Application;

import java.lang.foreign.MemorySegment;

/**
 * This is the Notepad application class. It contains the main method that will
 * create and run the application.
 */
public class Notepad extends Application {

    public static void main(String[] args) {
        var app = Notepad.create();
        app.run(args);
    }

    // Register the Notepad class
    private static final Type gtype = Types.register(Notepad.class);

    public static Type getType() {
        return gtype;
    }

    // Constructor
    public static Notepad create() {
        return GObject.newInstance(
                getType(),
                "application-id", "io.github.jwharm.javagi.examples.Notepad",
                "flags", ApplicationFlags.DEFAULT_FLAGS,
                null);
    }

    // Memory-address constructor, should always be present
    public Notepad(MemorySegment address) {
        super(address);
    }

    /**
     * When the application is activated, create a new EditorWindow
     */
    @Override
    public void activate() {
        var win = this.getActiveWindow();
        if (win == null) {
            win = EditorWindow.create(this);
            win.setDefaultSize(600, 400);
        }
        win.present();
    }
}
