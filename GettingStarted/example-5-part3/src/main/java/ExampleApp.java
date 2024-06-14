import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.glib.List;
import org.gnome.glib.Type;
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

  public static ExampleApp create() {
    return GObject.newInstance(getType(),
        "application-id", "org.gtk.exampleapp",
        "flags", ApplicationFlags.HANDLES_OPEN,
        null);
  }
}
