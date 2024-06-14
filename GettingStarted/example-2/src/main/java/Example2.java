import org.gnome.gtk.*;
import org.gnome.gio.ApplicationFlags;

public class Example2 {

  private static void printHello() {
    System.out.println("Hello World");
  }

  private static void activate(Application app) {
    // create a new window, and set its title
    Window window = new ApplicationWindow(app);
    window.setTitle("Window");
    
    // Here we construct the container that is going pack our buttons
    Grid grid = new Grid();
    
    // Pack the container in the window
    window.setChild(grid);
    
    Button button = Button.withLabel("Button 1");
    button.onClicked(Example2::printHello);
    
    // Place the first button in the grid cell (0, 0), and make it fill
    // just 1 cell horizontally and vertically (ie no spanning)
    grid.attach(button, 0, 0, 1, 1);
    
    button = Button.withLabel("Button 2");
    button.onClicked(Example2::printHello);
    
    // Place the second button in the grid cell (1, 0), and make it fill
    // just 1 cell horizontally and vertically (ie no spanning)
    grid.attach(button, 1, 0, 1, 1);
    
    button = Button.withLabel("Quit");
    button.onClicked(window::destroy);

    // Place the Quit button in the grid cell (0, 1), and make it
    // span 2 columns.
    grid.attach(button, 0, 1, 2, 1);

    window.present();
  }

  public static void main(String[] args) {
    Application app = new Application("org.gtk.example", ApplicationFlags.DEFAULT_FLAGS);
    app.onActivate(() -> activate(app));
    app.run(args);
  }
}

