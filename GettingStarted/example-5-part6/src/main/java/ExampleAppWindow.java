import io.github.jwharm.javagi.base.GErrorException;
import io.github.jwharm.javagi.base.Out;
import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.gio.File;
import org.gnome.gio.MenuModel;
import org.gnome.gio.SettingsBindFlags;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;
import org.gnome.gio.Settings;

import java.lang.foreign.MemorySegment;

@GtkTemplate(ui="/org/gtk/exampleapp/window.ui")
public class ExampleAppWindow extends ApplicationWindow {

  private static final Type gtype = TemplateTypes.register(ExampleAppWindow.class);

  @GtkChild
  public Stack stack;

  @GtkChild
  public MenuButton gears;

  public Settings settings;

  public static Type getType() {
    return gtype;
  }

  public ExampleAppWindow(MemorySegment address) {
    super(address);
  }

  public static ExampleAppWindow create(ExampleApp app) {
    return GObject.newInstance(getType(), "application", app, null);
  }

  @InstanceInit
  public void init() {
    var builder = GtkBuilder.fromResource("/org/gtk/exampleapp/gears-menu.ui");
    var menu = (MenuModel) builder.getObject("menu");
    gears.setMenuModel(menu);

    settings = new Settings("org.gtk.exampleapp");
    settings.bind("transition", stack, "transition-type", SettingsBindFlags.DEFAULT);
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
    var buffer = view.getBuffer();

    try {
      var contents = new Out<byte[]>();
      if (file.loadContents(null, contents, null)) {
        String str = new String(contents.get());
        buffer.setText(str, str.length());
      }
    } catch (GErrorException e) {
      throw new RuntimeException(e);
    }

    var tag = buffer.createTag(null, null);
    settings.bind("font", tag, "font", SettingsBindFlags.DEFAULT);
    TextIter startIter = new TextIter();
    TextIter endIter = new TextIter();
    buffer.getStartIter(startIter);
    buffer.getEndIter(endIter);
    buffer.applyTag(tag, startIter, endIter);
  }
}
