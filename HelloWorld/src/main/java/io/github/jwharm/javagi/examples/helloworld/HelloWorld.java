package io.github.jwharm.javagi.examples.helloworld;

import org.gnome.gtk.*;
import org.gnome.gio.ApplicationFlags;

public class HelloWorld {

    public static void main(String[] args) {
        new HelloWorld(args);
    }
    
    private final Application app;
    
    public HelloWorld(String[] args) {
        app = new Application("my.example.HelloApp", ApplicationFlags.DEFAULT_FLAGS);
        app.onActivate(this::activate);
        app.run(args);
    }
    
    public void activate() {
        var window = new ApplicationWindow(app);
        window.setTitle("GTK from Java");
        window.setDefaultSize(300, 200);
        
        var box = Box.builder()
            .setOrientation(Orientation.VERTICAL)
            .setHalign(Align.CENTER)
            .setValign(Align.CENTER)
            .build();
        
        var button = Button.withLabel("Hello world!");
        button.onClicked(window::close);
        
        box.append(button);
        window.setChild(box);
        window.present();
    }
}

