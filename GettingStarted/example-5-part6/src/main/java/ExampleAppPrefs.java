import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.Types;
import org.gnome.gio.Settings;
import org.gnome.gio.SettingsBindFlags;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.ComboBoxText;
import org.gnome.gtk.Dialog;
import org.gnome.gtk.FontButton;

import java.lang.foreign.MemorySegment;

@GtkTemplate(ui="/org/gtk/exampleapp/prefs.ui")
public class ExampleAppPrefs extends Dialog {

  private static final Type gtype = Types.register(ExampleAppPrefs.class);

  public static Type getType() {
    return gtype;
  }

  public ExampleAppPrefs(MemorySegment address) {
    super(address);
  }

  @GtkChild
  public FontButton font;

  @GtkChild
  public ComboBoxText transition;

  Settings settings;

  @InstanceInit
  public void init() {
    settings = new Settings("org.gtk.exampleapp");
    settings.bind("font", font, "font", SettingsBindFlags.DEFAULT);
    settings.bind("transition", transition, "active-id", SettingsBindFlags.DEFAULT);
  }

  public static ExampleAppPrefs create(ExampleAppWindow win) {
    return GObject.newInstance(getType(),
        "transient-for", win,
        "use-header-bar", 1,
        null);
  }
}
