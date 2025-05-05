package io.github.jwharm.javagi.examples.notepad;

import org.gnome.gio.ApplicationFlags;
import org.gnome.gtk.Application;

/**
 * This is the Notepad application class. It contains the main method that will
 * create and run the application.
 */
public class Notepad extends Application {

    public static void main(String[] args) {
        var app = new Notepad();
        app.run(args);
    }

    // Constructor
    public Notepad() {
        setApplicationId("my.example.Notepad");
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
            win.setDefaultSize(600, 400);
        }
        win.present();
    }
}
