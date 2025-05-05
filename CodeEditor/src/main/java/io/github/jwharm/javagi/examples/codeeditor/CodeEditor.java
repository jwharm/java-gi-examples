package io.github.jwharm.javagi.examples.codeeditor;

import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;

public class CodeEditor extends Application {

    public static void main(String[] args) {
        var app = new CodeEditor();
        app.run(args);
    }

    public CodeEditor () {
        setApplicationId("io.github.jwharm.javagi.examples.CodeEditor");
        setFlags(ApplicationFlags.DEFAULT_FLAGS);
    }

    /**
     * When the application is activated, create a new EditorWindow
     */
    @Override
    public void activate() {
        var win = this.getActiveWindow();
        if (win == null) {
            win = new EditorWindow(this);
        }
        win.setDefaultSize(600, 400);
        win.present();
    }
}
