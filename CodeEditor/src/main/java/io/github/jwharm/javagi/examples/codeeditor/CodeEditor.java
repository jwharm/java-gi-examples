package io.github.jwharm.javagi.examples.codeeditor;

import io.github.jwharm.javagi.gobject.types.Types;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.gtk.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

public class CodeEditor extends Application {

    public static void main(String[] args) {
        // Register the CodeEditor and EditorWindow types
        Types.register(CodeEditor.class);
        TemplateTypes.register(EditorWindow.class);

        var app = CodeEditor.create();
        app.run(args);
    }

    // Constructor
    public static CodeEditor create() {
        CodeEditor app = GObject.newInstance(CodeEditor.class);
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
