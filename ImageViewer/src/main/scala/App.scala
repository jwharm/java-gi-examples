import org.gnome.adw.Application
import org.gnome.gio.ApplicationFlags

class App extends Application("org.poach3r.Images", ApplicationFlags.DEFAULT_FLAGS):
  onActivate { () =>
    Window(this).present()
  }