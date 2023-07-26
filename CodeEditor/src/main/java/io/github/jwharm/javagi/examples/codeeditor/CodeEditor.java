package io.github.jwharm.javagi.examples.codeeditor;

import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

public class CodeEditor extends Application {

    public static void main(String[] args) {
        var app = CodeEditor.create();
        app.run(args);
    }

    // Register the CodeEditor class
    private static final Type gtype = Types.register(CodeEditor.class);

    public static Type getType() {
        return gtype;
    }

    // Constructor
    public static CodeEditor create() {
        CodeEditor app = GObject.newInstance(getType());
        app.setApplicationId("io.github.jwharm.javagi.examples.CodeEditor");
        app.setFlags(ApplicationFlags.DEFAULT_FLAGS);
        return app;
    }

    // Memory-address constructor, should always be present
    public CodeEditor(MemorySegment address) {
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
        }
        win.setDefaultSize(600, 400);
        win.present();
    }
}
