# Examples for Java-GI

This repository contains small example applications made with [Java-GI](https://github.com/jwharm/java-gi). Each example can be separately built and run with `gradle run`:

### Hello World

A typical ["Hello World" example](https://github.com/jwharm/java-gi-examples/tree/main/HelloWorld) that displays a Gtk window with a button. When you click the button, the application quits.

### Hello World - template based

This is a bit more complete ["Hello World" example](https://github.com/jwharm/java-gi-examples/tree/main/HelloTemplate) that is based on the default application that GNOME Builder generates for new Vala projects. It uses Gtk composite template classes to define the user interface in XML files. It also includes a Flatpak manifest and instructions to install and run the example as a Flatpak application.

### Calculator

A [basic calculator](https://github.com/jwharm/java-gi-examples/tree/main/Calculator) that uses Gtk and LibAdwaita. There's a header bar, and a grid-based layout for the buttons. The app reacts to key presses as expected.

### List integration

This example [demonstrates](https://github.com/jwharm/java-gi-examples/tree/main/ListViewer) how you can use a Java ArrayList to implement the GListModel interface, which is central to all modern Gtk list widgets.

### Notepad

A very basic Adwaita [plain-text editor](https://github.com/jwharm/java-gi-examples/tree/main/Notepad), that can load and save files using GIO.

### Code editor

A [source code editor](https://github.com/jwharm/java-gi-examples/tree/main/CodeEditor). It is mostly the same application as the Notepad example above, but this one uses GtkSourceview as the text widget and enables line numbers and syntax highlighting.

### Peg Solitaire

This [game](https://github.com/jwharm/java-gi-examples/tree/main/PegSolitaire) is a direct port of the Peg-Solitaire drag-and-drop example in the Gtk Demo.

### WebkitGtk Web browser

This example creates a very basic [web browser](https://github.com/jwharm/java-gi-examples/tree/main/Browser) using WebkitGtk. It was inspired by the browser example in GNOME Workbench.

### WebkitGtk Javascript callback

This example is a [small WebkitGtk application](https://github.com/jwharm/java-gi-examples/tree/main/Javascript) that runs a Java callback function from a Javascript function in the webpage.

### Mediastream

The [Mediastream example](https://github.com/jwharm/java-gi-examples/tree/main/MediaStream) is ported from GtkDemo. It paints a simple image using Cairo, and then rotates the image in a GtkVideo widget. The Cairo draw commands use the [Cairo Java bindings](https://github.com/jwharm/cairo-java-bindings).

### GStreamer sound player

This example [demonstrates how to use GStreamer](https://github.com/jwharm/java-gi-examples/tree/main/PlaySound) and is ported from the GStreamer tutorials. It creates a GStreamer pipeline that will play sound from an Ogg Vorbis file.

### GStreamer screen recorder

A command-line [screen recording](https://github.com/jwharm/java-gi-examples/tree/main/ScreenRecorder) utility that records the screen for 5 seconds into an Ogg/Theora file. It also demonstrates how to add a Tee and an AppSink to the pipeline so you can trigger custom actions on the recorded data.


