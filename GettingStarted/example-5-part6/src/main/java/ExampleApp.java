import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.List;
import org.gnome.glib.Type;
import org.gnome.glib.Variant;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Application;
import org.gnome.gtk.Window;
import java.lang.foreign.MemorySegment;

public class ExampleApp extends Application {

  private static final Type gtype = Types.register(ExampleApp.class);

  public static Type getType() {
    return gtype;
  }

  public ExampleApp(MemorySegment address) {
    super(address);
  }

  @Override
  public void activate() {
    ExampleAppWindow win = ExampleAppWindow.create(this);
    win.present();
  }

  @Override
  public void open(File[] files, String hint) {
    ExampleAppWindow win;
    List<Window> windows = super.getWindows();
    if (!windows.isEmpty())
      win = (ExampleAppWindow) windows.getFirst();
    else
      win = ExampleAppWindow.create(this);

    for (File file : files)
      win.open(file);

    win.present();
  }

  public void preferencesActivated(Variant parameter) {
    ExampleAppWindow win = (ExampleAppWindow) getActiveWindow();
    ExampleAppPrefs prefs = ExampleAppPrefs.create(win);
    prefs.present();
  }

  public void quitActivated(Variant parameter) {
    super.quit();
  }

  @Override
  public void startup() {
    super.startup();

    var preferences = new SimpleAction("preferences", null);
    preferences.onActivate(this::preferencesActivated);
    addAction(preferences);

    var quit = new SimpleAction("quit", null);
    quit.onActivate(this::quitActivated);
    addAction(quit);

    String[] quitAccels = new String[]{"<Ctrl>q"};
    setAccelsForAction("app.quit", quitAccels);
  }

  public static ExampleApp create() {
    return GObject.newInstance(getType(),
        "application-id", "org.gtk.exampleapp",
        "flags", ApplicationFlags.HANDLES_OPEN,
        null);
  }
}
