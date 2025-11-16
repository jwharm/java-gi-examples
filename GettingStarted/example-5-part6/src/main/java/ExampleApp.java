import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.List;
import org.gnome.glib.Variant;
import org.gnome.gtk.Application;
import org.gnome.gtk.Window;

public class ExampleApp extends Application {

  @Override
  public void activate() {
    ExampleAppWindow win = new ExampleAppWindow(this);
    win.present();
  }

  @Override
  public void open(File[] files, String hint) {
    ExampleAppWindow win;
    List<Window> windows = super.getWindows();
    if (!windows.isEmpty())
      win = (ExampleAppWindow) windows.getFirst();
    else
      win = new ExampleAppWindow(this);

    for (File file : files)
      win.open(file);

    win.present();
  }

  public void preferencesActivated(Variant parameter) {
    ExampleAppWindow win = (ExampleAppWindow) getActiveWindow();
    ExampleAppPrefs prefs = new ExampleAppPrefs(win);
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

  public ExampleApp() {
    setApplicationId("org.gtk.exampleapp");
    setFlags(ApplicationFlags.HANDLES_OPEN);
  }
}
