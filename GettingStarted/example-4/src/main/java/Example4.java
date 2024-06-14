import io.github.jwharm.javagi.base.GErrorException;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gtk.*;

public class Example4 {

  private static void printHello() {
    System.out.println("Hello World");
  }

  private static void activate(Application app) {
    // Construct a GtkBuilder instance and load our UI description
    GtkBuilder builder = new GtkBuilder();
    try {
      builder.addFromFile("src/main/resources/builder.ui");
    } catch (GErrorException ignored) {}

    // Connect signal handlers to the constructed widgets.
    Window window = (Window) builder.getObject("window");
    window.setApplication(app);

    Button button = (Button) builder.getObject("button1");
    button.onClicked(Example4::printHello);

    button = (Button) builder.getObject("button2");
    button.onClicked(Example4::printHello);

    button = (Button) builder.getObject("quit");
    button.onClicked(window::destroy);

    window.setVisible(true);
  }

  public static void main(String[] args) {
    Application app = new Application("org.gtk.example", ApplicationFlags.DEFAULT_FLAGS);
    app.onActivate(() -> activate(app));
    app.run(args);
  }
}
