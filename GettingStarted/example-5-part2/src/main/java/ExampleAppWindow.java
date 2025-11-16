import org.javagi.gtk.annotations.GtkTemplate;
import org.gnome.gio.File;
import org.gnome.gtk.ApplicationWindow;

@GtkTemplate(ui="/org/gtk/exampleapp/window.ui")
public class ExampleAppWindow extends ApplicationWindow {

  public ExampleAppWindow(ExampleApp app) {
    setApplication(app);
  }

  public void open(File file) {
  }
}
