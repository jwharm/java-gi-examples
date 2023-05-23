## Hello Template

This example is a simple "Hello World" Gtk application using a UI template. It is based on the template in GNOME Builder.

The application needs a few fixes that will become available in Java-GI version 0.6 only. You can also run it with a locally compiled Java-GI build from the github main branch, and publish the artifacts to mavenLocal. 

Afterwards, to run the example, clone the java-gi-examples repository, navigate to the `HelloTemplate` folder, and execute `gradle run`.

If you change the ui files, you must run `glib-compile-resources` on them, or else they will not be visible in the application.
