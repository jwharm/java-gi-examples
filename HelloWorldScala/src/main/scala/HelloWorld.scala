import org.gnome.gio.ApplicationFlags
import org.gnome.gtk.{Align, Application, ApplicationWindow, Box, Button, HeaderBar, Orientation}

object HelloWorld:
  def main(args: Array[String] = Array()): Unit = new Application("my.example.HelloApp", ApplicationFlags.DEFAULT_FLAGS) {
    onActivate(() => HelloWorld().activate(this))
  }.run(args)

class HelloWorld:
  private def activate(app: Application): Unit =
    lazy val window: ApplicationWindow = new ApplicationWindow(app) {
      setTitle("Gtk from Scala")
      setDefaultSize(300, 200)
      setChild(box)
      setTitlebar(HeaderBar())
    }

    lazy val box: Box = new Box(Orientation.VERTICAL, 1) {
      val button: Button = new Button() {
        setLabel("Hello, world!")
        onClicked(() => window.close())
      }

      setHalign(Align.CENTER)
      setValign(Align.CENTER)
      append(button)
    }

    window.present()