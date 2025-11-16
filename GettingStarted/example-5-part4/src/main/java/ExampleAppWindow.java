import org.javagi.base.GErrorException;
import org.javagi.base.Out;
import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;
import org.gnome.gio.File;
import org.gnome.gio.MenuModel;
import org.gnome.gtk.*;

@GtkTemplate(ui="/org/gtk/exampleapp/window.ui")
public class ExampleAppWindow extends ApplicationWindow {

  @GtkChild
  public Stack stack;

  @GtkChild
  public MenuButton gears;

  public ExampleAppWindow(ExampleApp app) {
    setApplication(app);
  }

  @InstanceInit
  public void init() {
    var builder = GtkBuilder.fromResource("/org/gtk/exampleapp/gears-menu.ui");
    var menu = (MenuModel) builder.getObject("menu");
    gears.setMenuModel(menu);
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
