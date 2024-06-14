import org.gnome.gtk.*;
import org.gnome.gio.ApplicationFlags;

public class Example1 {

  private static void printHello() {
    System.out.println("Hello World");
  }

  private static void activate(Application app) {
    Window window = new ApplicationWindow(app);
    window.setTitle("Window");
    window.setDefaultSize(200, 200);

    Box box = new Box(Orientation.VERTICAL, 0);
    box.setHalign(Align.CENTER);
    box.setValign(Align.CENTER);

    window.setChild(box);

    Button button = Button.withLabel("Hello World");

    button.onClicked(Example1::printHello);
    button.onClicked(window::destroy);

    box.append(button);

    window.present();
  }

  public static void main(String[] args) {
    Application app = new Application("org.gtk.example", ApplicationFlags.DEFAULT_FLAGS);
    app.onActivate(() -> activate(app));
    app.run(args);
  }
}

