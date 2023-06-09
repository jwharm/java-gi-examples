## Source code editor

This example is a small Adwaita application to edit text in a GtkSourceView component.

It is very much like the Notepad example, but this one uses the`GtkSourceView` instead of the `GtkTextView` widget, with line numbers and syntax highlighting enabled.

To run the example, clone the repository, navigate to the `CodeEditor` folder, and execute `gradle run`.

Note: While I was running this example on Fedora 38, the library `gtksourceview-5` could not be found. In my case, this was fixed with a symlink from the `/usr/lib64/libgtksourceview-5.so.0` file to  `libgtksourceview-5.so`, like this:

```sh
cd /usr/lib64
sudo ln -s libgtksourceview-5.so.0 libgtksourceview-5.so
```
