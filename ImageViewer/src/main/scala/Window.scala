import org.javagi.base.GErrorException
import org.gnome.adw.{ApplicationWindow, HeaderBar}
import org.gnome.gio.AsyncResult
import org.gnome.gobject.GObject
import org.gnome.gtk.{Picture, Button, Box, Orientation, FileDialog, FileFilter}

import java.lang.foreign.MemorySegment

class Window(app: App) extends ApplicationWindow(app):
  this.setTitle("None")
  this.setContent(content(Picture()))

  private def header(): HeaderBar =
    val openButton = new Button() {
      setIconName("document-open-symbolic")
      addCssClass("suggested-action")
      onClicked { () =>
        openFileDialogue()
      }
    }

    val header = HeaderBar()
    header.packStart(openButton)
    header

  private def content(pic: Picture): Box = new Box(Orientation.VERTICAL, 0) {
    append(header())
    append(pic)
  }

  private def openFileDialogue(): Unit =
    val dialog = FileDialog.builder()
      .setDefaultFilter(FileFilter.builder()
        .setSuffixes(Array("png", "jpg", "jpeg", "webp", "svg"))
        .build()
      ).build()

    dialog.open(this, null, (_: GObject, result: AsyncResult, _: MemorySegment) => {
      try
        val pic = Picture.forFile(dialog.openFinish(result))
        setContent(content(pic))
        setTitle(pic.getFile.getBasename)
      catch case _: GErrorException => println("Failed to find file.")
    })