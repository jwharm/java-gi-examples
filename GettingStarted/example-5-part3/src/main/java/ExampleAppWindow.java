import io.github.jwharm.javagi.base.GErrorException;
import io.github.jwharm.javagi.base.Out;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.gio.File;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.ApplicationWindow;
import org.gnome.gtk.ScrolledWindow;
import org.gnome.gtk.Stack;
import org.gnome.gtk.TextView;

import java.lang.foreign.MemorySegment;

@GtkTemplate(ui="/org/gtk/exampleapp/window.ui")
public class ExampleAppWindow extends ApplicationWindow {

  @GtkChild
  public Stack stack;

  public ExampleAppWindow(ExampleApp app) {
    setApplication(app);
  }

  public void open(File file) {
    String basename = file.getBasename();

    var scrolled = new ScrolledWindow();
    scrolled.setHexpand(true);
    scrolled.setVexpand(true);
    var view = new TextView();
    view.setEditable(false);
    view.setCursorVisible(false);
    scrolled.setChild(view);
    stack.addTitled(scrolled, basename, basename);

    try {
      var contents = new Out<byte[]>();
      if (file.loadContents(null, contents, null)) {
        var buffer = view.getBuffer();
        String str = new String(contents.get());
        buffer.setText(str, str.length());
      }
    } catch (GErrorException e) {
      throw new RuntimeException(e);
    }
  }
}
