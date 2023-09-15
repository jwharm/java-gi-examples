package io.github.jwharm.javagi.examples.hellotemplate;

import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gobject.annotations.RegisteredType;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.AboutWindow;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Type;
import org.gnome.glib.Variant;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

/**
 * HelloApplication is derived from AdwApplication. The class is registered as
 * a new GType.
 * <p>
 * The method {@link #activate()} is registered in the GTypeClass
 * as a virtual method, so the GType system will call it when the application
 * is activated.
 * <p>
 * The method {@link #init()} is called during instance initialization of
 * the new GObject instance.
 */
@RegisteredType(name="HelloApplication")
public class HelloApplication extends Application {

    /**
     * Register the class as a new, derived GType
     */
    public static Type gtype = Types.register(HelloApplication.class);

    /**
     * Default memory-address constructor that all java-gi classes must have
     * @param address the address of a native instance
     */
    public HelloApplication(MemorySegment address) {
        super(address);
    }

    /**
     * This is the constructor of the HelloApplication class.
     * It needs to be a static factory method, because the actual instance
     * is created by {@link GObject#newInstance(Type)} and calls the {@link #init()}
     * method before it returns, which would be impossible in a regular constructor with a
     * {@code super(type)} call.
     * @return a new HelloApplication instance
     */
    public static HelloApplication create() {
        HelloApplication app = GObject.newInstance(gtype);
        app.setApplicationId("io.github.jwharm.javagi.examples.HelloTemplate");
        app.setFlags(ApplicationFlags.DEFAULT_FLAGS);
        return app;
    }

    /**
     * This method is called during construction of the new GObject instance.
     * The name "construct" can be freely chosen; the {@code @InstanceInit} annotation
     * marks it as an instance init function.
     */
    @InstanceInit
    public void init() {
        var about = new SimpleAction("about", null);
        about.onActivate(this::onAboutAction);
        addAction(about);

        var preferences = new SimpleAction("preferences", null);
        preferences.onActivate(this::onPreferencesAction);
        addAction(preferences);

        var greet = new SimpleAction("greet", null);
        greet.onActivate(this::onGreetAction);
        addAction(greet);

        var quit = new SimpleAction("quit", null);
        quit.onActivate($ -> quit());
        addAction(quit);
        this.setAccelsForAction("app.quit", new String[]{"<primary>q"});
    }

    /**
     * Virtual method overrides are automatically registered in the GObject type class.
     * This means the activate() method is automatically recognized by Gtk and will be
     * executed during application startup.
     */
    @Override
    public void activate() {
        var win = this.getActiveWindow();
        if (win == null) {
            win = HelloWindow.create(this);
        }
        win.present();
    }

    // The following methods are executed by the ActionEntries defined above.

    private void onAboutAction(Variant parameter) {
        String[] developers = { "John Doe", "Jane Doe" };
        var about = AboutWindow.builder()
            .transientFor(this.getActiveWindow())
            .applicationName("HelloTemplate")
            .applicationIcon("io.github.jwharm.javagi.examples.HelloTemplate")
            .developerName("James Random Hacker")
            .developers(developers)
            .version("0.1.0")
            .copyright("© 2023 Yoyodyne, Inc")
            .build();
        about.present();
    }

    private void onPreferencesAction(Variant parameter) {
        System.out.println("app.preferences action activated");
    }

    private void onGreetAction(Variant parameter) {
        var win = (HelloWindow) this.getActiveWindow();
        if (win != null) {
            win.label.setLabel("Hello again!");
        }
    }
}
