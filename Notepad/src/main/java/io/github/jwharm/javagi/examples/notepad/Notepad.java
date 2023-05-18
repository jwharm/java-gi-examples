package io.github.jwharm.javagi.examples.notepad;

import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.HeaderBar;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.gio.FileCreateFlags;
import org.gnome.gtk.*;

import io.github.jwharm.javagi.base.GErrorException;
import io.github.jwharm.javagi.base.Out;

public class Notepad extends Application {

	// Main method: runs the application
    public static void main(String[] args) {
        var app = new Notepad("io.github.jwharm.samples.Notepad", ApplicationFlags.NON_UNIQUE);
        app.onActivate(app::activate);
        app.run(args);
    }
    
    // The application main window
    ApplicationWindow window;
    
    // The text editor widget
    TextView textView;
    
    // File handle of an opened file
    File file;
    
    // Indicates whether the open document has been modified
    boolean modified = false;
    
    // Window title (file name or "Unnamed")
    String title = "Unnamed";
    
    public Notepad(String applicationId, ApplicationFlags flags) {
        super(applicationId, flags);
    }
    
    public void activate() {
    	// Create window
        window = new ApplicationWindow(this);
        window.setDefaultSize(600, 400);
        window.setTitle(title);
        
        // Create layout box
        var vbox = new Box(Orientation.VERTICAL, 0);
        
        // Create header bar
        var headerBar = new HeaderBar();
        vbox.append(headerBar);
        
        // Create the editor widget and set the font style to monospace
        textView = new TextView();
        textView.setMonospace(true);

        // The text editor should be scrollable
        var scrolledWindow = new ScrolledWindow();
        scrolledWindow.setChild(textView);
        scrolledWindow.setVexpand(true);
        vbox.append(scrolledWindow);
        
        // Create buttons for 'new', 'open' and 'save' actions.
        // The builder pattern is used to construct the objects.
        // Each button gets an icon (from the default icons included in GTK),
        // and the 'clicked' signal is connected to a method in this class.
        
        Button newButton = Button.builder()
                .setLabel("New...")
                .setIconName("document-new-symbolic")
                .build();
        newButton.onClicked(this::clear);
        headerBar.packStart(newButton);
        
        Button openButton = Button.builder()
                .setLabel("Open...")
                .setIconName("document-open-symbolic")
                .build();
        openButton.onClicked(this::open);
        headerBar.packStart(openButton);
        
        Button saveButton = Button.builder()
                .setLabel("Save")
                .setIconName("document-save-symbolic")
                .build();
        saveButton.onClicked(this::save);
        headerBar.packEnd(saveButton);
        
        // Add the layout box to the window and show the results
        window.setContent(vbox);
        window.present();
        
        // When the 'modified-changed' signal is emitted, update the window title
        textView.getBuffer().onModifiedChanged(() -> {
        	modified = textView.getBuffer().getModified();
        	updateWindowTitle();
        });
        
        // Make sure the text field has the keyboard focus
        textView.grabFocus();
    }
    
    // Use UTF 2022 (BULLET) character to indicate that the text has been modified
    private void updateWindowTitle() {
    	window.setTitle((modified ? ("• ") : "") + title);
    }
    
    // New unnamed file
    public void clear() {
        file = null;
    	title = "Unnamed";
    	textView.getBuffer().setText("", 0);
    	textView.getBuffer().setModified(false);
    	modified = false;
    	updateWindowTitle();
    	textView.grabFocus();
    }
    
    // Open a file (using FileChooserNative)
    public void open() {

        var dialog = new FileDialog();
        dialog.open(window, null, (obj, res, d) -> {
            try {
                file = dialog.openFinish(res);
                if (file == null) {
                    return;
                }

                // Read file and put contents in the textview's text buffer. The read is done asynchronously.
                file.loadContentsAsync(null, (object, result, data) -> {
                    try {
                        // The 'contents' and 'length' parameters are out-parameters. Because Java always does
                        // pass-by-value, we need to wrap these into Out<> objects.
                        Out<byte[]> contents = new Out<>();
                        if (file.loadContentsFinish(result, contents, null)) {
                            title = file.getBasename();
                            updateWindowTitle();
                            textView.getBuffer().setText(new String(contents.get()), contents.get().length);
                            textView.grabFocus();
                        }
                    } catch (GErrorException e) {
                        System.err.println("Error: " + e.getMessage());
                    }
                });
            } catch (GErrorException ignored) {
            }
        });
    }
    
    // Save a file. If the filename isn't known yet, show a 'Save file' dialog.
    public void save() {
    	if (file == null) {
            var dialog = new FileDialog();
            dialog.save(window, null, (obj, res, d) -> {
                try {
                    file = dialog.saveFinish(res);
                    if (file == null) {
                        return;
                    }

                    title = file.getBasename();
                    textView.getBuffer().setModified(false);
                    modified = false;
                    updateWindowTitle();
                    textView.grabFocus();
                    write();
                } catch (GErrorException ignored) {
                }
            });
    	} else {
    		// Reset the 'modified' indicator and update the window title
	    	textView.getBuffer().setModified(false);
	    	modified = false;
			updateWindowTitle();
	    	textView.grabFocus();
    		write();
    	}
    }

    // This method is used from save() to write the contents to the file.
    private void write() {
    	TextIter start = TextIter.allocate();
    	TextIter end = TextIter.allocate();
    	textView.getBuffer().getBounds(start, end);
    	byte[] contents = textView.getBuffer().getText(start, end, false).getBytes();
    	try {
    		// This would better be executed asynchronously with File.replaceContentsAsync().
			file.replaceContents(contents, "", false, FileCreateFlags.NONE, null, null);
		} catch (GErrorException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
