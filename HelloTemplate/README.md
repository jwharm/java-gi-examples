## Hello Template

This example is a simple "Hello World" Gtk application using an XML template. 
It is inspired by the Vala project template generated by GNOME Builder.

The application needs a few fixes that will become available in Java-GI version 
0.6. For now, you can only run it with a locally compiled Java-GI build from 
the Github main branch. If you publish the artifacts to mavenLocal, the 
`gradle.build` file in the `HelloTemplate` folder should be able to find them 
when you execute `gradle run`.

The ui resource files are located in the `src/main/resources/` folder. They 
need to be compiled with `glib-resource-compiler` before they can be used. The 
Gradle build script is configured to run `glib-compile-resources` before the 
Java code is compiled and run, so please make sure the Gtk development tools 
are installed.