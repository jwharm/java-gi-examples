import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.gio.File;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.ApplicationWindow;
import java.lang.foreign.MemorySegment;

@GtkTemplate(ui="/org/gtk/exampleapp/window.ui")
public class ExampleAppWindow extends ApplicationWindow {

  private static final Type gtype = TemplateTypes.register(ExampleAppWindow.class);

  public static Type getType() {
    return gtype;
  }

  public ExampleAppWindow(MemorySegment address) {
    super(address);
  }

  public static ExampleAppWindow create(ExampleApp app) {
    return GObject.newInstance(getType(), "application", app, null);
  }

  public void open(File file) {
  }
}
