package my.example.hellotemplate;

import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.ApplicationWindow;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Application;
import org.gnome.gtk.Label;

import java.lang.foreign.MemorySegment;

/**
 * The {@code @GtkTemplate} annotation marks HelloWindow as a Gtk composite template
 * class. The user interface is defined in the ui file, and the application logic is
 * implemented in Java.
 */
@GtkTemplate(name="HelloWindow", ui="/my/example/window.ui")
public class HelloWindow extends ApplicationWindow {

    // Register the class as a new, derived GType
    static {
        TemplateTypes.register(HelloWindow.class);
    }

    /**
     * Default memory-address constructor that all java-gi classes must have
     * @param address the address of a native instance
     */
    public HelloWindow(MemorySegment address) {
        super(address);
    }

    /**
     * This field is automatically set to the GtkLabel instance defined in the ui file.
     */
    @GtkChild
    public Label label;

    /**
     * The static factory method to construct a new HelloWindow instance.
     * @param app the HelloApplication instance
     * @return the new HelloWindow instance
     */
    public static HelloWindow create(Application app) {
        HelloWindow win = GObject.newInstance(HelloWindow.class);
        win.setApplication(app);
        return win;
    }
}
