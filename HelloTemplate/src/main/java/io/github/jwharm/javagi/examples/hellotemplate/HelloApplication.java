package io.github.jwharm.javagi.examples.hellotemplate;

import io.github.jwharm.javagi.annotations.InstanceInit;
import io.github.jwharm.javagi.annotations.RegisteredType;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.adw.AboutWindow;
import org.gnome.adw.Application;
import org.gnome.gio.ActionEntry;
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
 * The method {@link #construct()} is called during instance initialization of
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
     * is created by {@link GObject#newInstance(Type)} and calls the {@link #construct()}
     * method before it returns, which would be impossible in a regular constructor with a
     * {@code super(type)} call.
     * @return a new HelloApplication instance
     */
    public static HelloApplication newInstance() {
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
    public void construct() {
        // Each ActionEntry is a struct. We only need to fill the name and action fields, and leave the other fields empty (null).
        ActionEntry[] entries = {
            ActionEntry.allocate("about", this::onAboutAction, null, null, null),
            ActionEntry.allocate("preferences", this::onPreferencesAction, null, null, null),
            ActionEntry.allocate("greet", this::onGreetAction, null, null, null),
            ActionEntry.allocate("quit", (action, variant) -> this.quit(), null, null, null)
        };
        this.addActionEntries(entries, null);
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
            win = HelloWindow.newInstance(this);
        }
        win.present();
    }

    // The following methods are executed by the ActionEntries defined above.

    private void onAboutAction(SimpleAction action, Variant parameter) {
        String[] developers = { "John Doe", "Jane Doe" };
        var about = AboutWindow.builder()
            .setTransientFor(this.getActiveWindow())
            .setApplicationName("HelloTemplate")
            .setApplicationIcon("io.github.jwharm.javagi.examples.HelloTemplate")
            .setDeveloperName("James Random Hacker")
            .setDevelopers(developers)
            .setVersion("0.1.0")
            .setCopyright("© 2023 Yoyodyne, Inc")
            .build();
        about.present();
    }

    private void onPreferencesAction(SimpleAction action, Variant parameter) {
        System.out.println("app.preferences action activated");
    }

    private void onGreetAction(SimpleAction action, Variant parameter) {
        var win = (HelloWindow) this.getActiveWindow();
        if (win != null) {
            win.label.setLabel("Hello again!");
        }
    }
}
