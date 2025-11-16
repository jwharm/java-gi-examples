import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;
import org.gnome.gio.Settings;
import org.gnome.gio.SettingsBindFlags;
import org.gnome.gtk.ComboBoxText;
import org.gnome.gtk.Dialog;
import org.gnome.gtk.FontButton;

@GtkTemplate(ui="/org/gtk/exampleapp/prefs.ui")
public class ExampleAppPrefs extends Dialog {

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

  public ExampleAppPrefs(ExampleAppWindow win) {
    setTransientFor(win);
  }
}
