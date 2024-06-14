import org.gnome.gtk.*;
import org.gnome.gio.ApplicationFlags;

public class Example0 {

  public static void main(String[] args) {
    Application app = new Application("org.gtk.example", ApplicationFlags.DEFAULT_FLAGS);
    app.onActivate(() -> {
      Window window = new ApplicationWindow(app);
      window.setTitle("Window");
      window.setDefaultSize(200, 200);
      window.present();
    });
    app.run(args);
  }
}

