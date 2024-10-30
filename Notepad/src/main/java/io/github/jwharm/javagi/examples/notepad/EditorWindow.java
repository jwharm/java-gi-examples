package io.github.jwharm.javagi.examples.notepad;

import io.github.jwharm.javagi.gobject.annotations.InstanceInit;
import io.github.jwharm.javagi.base.GErrorException;
import io.github.jwharm.javagi.base.Out;
import io.github.jwharm.javagi.gobject.types.Types;
import org.gnome.gio.File;
import org.gnome.gio.FileCreateFlags;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;

import java.lang.foreign.MemorySegment;

/**
 * The EditorWindow class contains a headerbar and the textview. The headerbar
 * contains buttons for new/open/save actions.
 */
public class EditorWindow extends ApplicationWindow {

    // The currently open file (or null when no file is open)
    private File file = null;

    // The textview component
    private TextView textview;

    // Register the EditorWindow class
    private static final Type gtype = Types.register(EditorWindow.class);

    public static Type getType() {
        return gtype;
    }

    // Constructor for a new EditorWindow
    public static EditorWindow create(Application application) {
        EditorWindow window = GObject.newInstance(getType());
        window.setApplication(application);
        window.present();

        // Make sure the text field has the keyboard focus.
        window.textview.grabFocus();

        return window;
    }

    // Memory-address constructor, should always be present
    public EditorWindow(MemorySegment address) {
        super(address);
    }

    /**
     * The @InstanceInit method is called during construction of a new EditorWindow instance.
     * It creates the window layout (the headerbar, buttons, and textview).
     */
    @InstanceInit
    public void init() {

        // Create the headerbar
        var header = new HeaderBar();
        super.setTitlebar(header);

        // Create the TextView and set the font style to monospace
        textview = new TextView();
        textview.setMonospace(true);

        // The window title contains a "modified" indicator, that is
        // updated by the "on-modified-change" signal of the text buffer.
        textview.getBuffer().onModifiedChanged(this::updateWindowTitle);

        // The textView should be scrollable.
        var scrolledWindow = ScrolledWindow.builder()
                .setChild(textview)
                .setVexpand(true)
                .build();
        super.setChild(scrolledWindow);

        // Create buttons for 'new', 'open' and 'save' actions.
        var newButton = Button.fromIconName("document-new-symbolic");
        newButton.onClicked(() -> whenSure(this::clear));
        header.packStart(newButton);

        var openButton = Button.fromIconName("document-open-symbolic");
        openButton.onClicked(() -> whenSure(this::open));
        header.packStart(openButton);

        var saveButton = Button.fromIconName("document-save-symbolic");
        saveButton.onClicked(this::save);
        header.packStart(saveButton);

        // Ask to save changes before closing the window.
        this.onCloseRequest(() -> {
            whenSure(this::destroy);
            return true;
        });

        // Show the results.
        updateWindowTitle();
    }

    /**
     * Updates the window title to a modified-indicator and the current filename.
     * When no file is open, the title is "Unnamed".
     */
    private void updateWindowTitle() {
        super.setTitle(
                (textview.getBuffer().getModified() ? ("• ") : "") +
                (file == null ? "Unnamed" : file.getBasename())
        );
    }

    /**
     * Runs {@code action} but, if the buffer is modified, asks to save
     * the modifications first.
     *
     * @param action the action to run after saving the modifications
     */
    private void whenSure(Runnable action) {
        // No modifications?
        if (! textview.getBuffer().getModified()) {
            action.run();
            return;
        }

        // Set up the confirmation dialog with three buttons:
        // 0 - Cancel, 1 - Discard, 2 - Save
        AlertDialog alert = AlertDialog.builder()
                .setModal(true)
                .setMessage("Save changes?")
                .setDetail("Do you want to save your changes?")
                .setButtons(new String[] {"Cancel", "Discard", "Save"})
                .setCancelButton(0)
                .setDefaultButton(2)
                .build();

        // Get dialog result
        alert.choose(this, null, (_, result, _) -> {
            try {
                int button = alert.chooseFinish(result);
                if (button == 0) return; // cancel
                if (button == 2) save(); // save
                action.run();
            } catch (GErrorException ignored) {} // user clicked cancel
        });
    }

    /**
     * "New" action: clear the editor buffer.
     * This could also open a new window, instead of cleaning the current buffer.
     */
    public void clear() {
        file = null;
    	textview.getBuffer().setText("", 0);
    	textview.getBuffer().setModified(false);
        textview.grabFocus();
    }

    /**
     * "Open" action: Load a file and show the contents in the editor.
     */
    public void open() {
        // Set up an Open File dialog.
        var dialog = new FileDialog();
        dialog.open(this, null, (_, result, _) -> {
            try {
                file = dialog.openFinish(result);
            } catch (GErrorException ignored) {} // used clicked cancel
            if (file == null) return;

            // Load the contents of the selected file.
            try {
                // The byte[] parameter is an out-parameter in the C API.
                // Create an empty Out<byte[]> object, and read its value afterward.
                Out<byte[]> contents = new Out<>();
                file.loadContents(null, contents, null);
                textview.getBuffer().setText(new String(contents.get()), contents.get().length);
                textview.getBuffer().setModified(false);
                textview.grabFocus();
            } catch (GErrorException e) {
                AlertDialog.builder()
                        .setModal(true)
                        .setMessage("Error reading from file")
                        .setDetail(e.getMessage())
                        .build()
                        .show(this);
            }
        });
    }

    /**
     * "Save" action: Show a file dialog (for new files) and call {@link #write()}
     */
    public void save() {
        if (file == null) {
            // Set up a Save File dialog.
            var dialog = new FileDialog();
            dialog.save(this, null, (_, result, _) -> {
                try {
                    file = dialog.saveFinish(result);
                    if (file == null) return;

                    // Write the textview buffer contents to the selected file.
                    write();
                    textview.getBuffer().setModified(false);
                    textview.grabFocus();
                } catch (GErrorException ignored) {} // used clicked cancel
            });
        } else {
            // Write the textview buffer contents to the file that was already open.
            write();
            textview.getBuffer().setModified(false);
            textview.grabFocus();
        }
    }

    /**
     * Helper function that writes editor contents to a file.
     */
    private void write() {
        try {
            // Get the contents of the textview buffer as a byte array
            TextIter start = new TextIter();
            TextIter end = new TextIter();
            textview.getBuffer().getBounds(start, end);
            byte[] contents = textview.getBuffer().getText(start, end, false).getBytes();

            // Write the byte array to the file
            file.replaceContents(contents, "", false, FileCreateFlags.NONE, null, null);
        } catch (GErrorException e) {
            AlertDialog.builder()
                    .setModal(true)
                    .setMessage("Error writing to file")
                    .setDetail(e.getMessage())
                    .build()
                    .show(this);
        }
    }
}
