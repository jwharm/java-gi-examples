package my.example.hellotemplate;

import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import org.gnome.adw.ApplicationWindow;
import org.gnome.gtk.Application;
import org.gnome.gtk.Label;

/**
 * The {@code @GtkTemplate} annotation marks HelloWindow as a Gtk composite
 * template class. The user interface is defined in the ui file, and the
 * application logic is implemented in Java.
 */
@GtkTemplate(name="HelloWindow", ui="/my/example/window.ui")
public class HelloWindow extends ApplicationWindow {
    /**
     * This field is set to the GtkLabel instance defined in the ui file.
     */
    @GtkChild
    public Label label;

    /**
     * Construct a new HelloWindow instance.
     * @param app the HelloApplication instance
     */
    public HelloWindow (Application app) {
        setApplication(app);
    }
}
