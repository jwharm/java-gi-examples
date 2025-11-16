package my.example.hellotemplate;

import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gobject.annotations.RegisteredType;
import org.gnome.adw.AboutDialog;
import org.gnome.adw.Application;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Variant;

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
     * This is the constructor of the HelloApplication class. When the instance
     * is created the {@link #init()}  method is automatically run.
     */
    public HelloApplication() {
        setApplicationId("my.example.HelloTemplate");
        setFlags(ApplicationFlags.DEFAULT_FLAGS);
    }

    /**
     * This method is called during construction of the new GObject instance.
     * The name "init" can be freely chosen; the {@code @InstanceInit} annotation
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
        quit.onActivate(_ -> quit());
        addAction(quit);
        setAccelsForAction("app.quit", new String[]{"<primary>q"});
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
            win = new HelloWindow(this);
        }
        win.present();
    }

    // The following methods are executed by the ActionEntries defined above.

    private void onAboutAction(Variant parameter) {
        String[] developers = { "John Doe", "Jane Doe" };
        var about = AboutDialog.builder()
            .setApplicationName("HelloTemplate")
            .setApplicationIcon("my.example.HelloTemplate")
            .setDeveloperName("James Random Hacker")
            .setDevelopers(developers)
            .setVersion("0.1.0")
            .setCopyright("© 2023 Yoyodyne, Inc")
            .build();
        about.present(this.getActiveWindow());
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
