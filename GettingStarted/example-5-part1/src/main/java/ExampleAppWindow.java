import org.gnome.gio.File;
import org.gnome.gtk.ApplicationWindow;

public class ExampleAppWindow extends ApplicationWindow {

  public ExampleAppWindow(ExampleApp app) {
    setApplication(app);
  }

  public void open(File file) {
  }
}
