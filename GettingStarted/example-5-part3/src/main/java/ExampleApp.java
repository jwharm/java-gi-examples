import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.glib.List;
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

  public ExampleApp() {
    setApplicationId("org.gtk.exampleapp");
    setFlags(ApplicationFlags.HANDLES_OPEN);
  }
}
