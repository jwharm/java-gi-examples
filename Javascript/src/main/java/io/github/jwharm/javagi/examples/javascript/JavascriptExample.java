package io.github.jwharm.javagi.examples.javascript;

import io.github.jwharm.javagi.base.UnsupportedPlatformException;
import org.gnome.gtk.*;
import org.gnome.gio.ApplicationFlags;
import org.gnome.webkit.*;
import org.gnome.webkit.jsc.Value;

/**
 * Small example app that demonstrates a callback from a Javascript script running
 * in a WebView that calls a Java method.
 */
public class JavascriptExample {

    public static void main(String[] args) {
        new JavascriptExample(args);
    }
    
    private final Application app;
    private ApplicationWindow window;

    // The javascript
    private static final String script = """
        (function(globalContext) {
            globalContext.document.getElementById("inputId").onclick = function () {
                var message = { theMessage : "Sent from Javascript" };
                window.webkit.messageHandlers["handlerId"].postMessage(message);
            };
        })(this)
        """;

    // The webpage
    private static final String html = """
        <!DOCTYPE html>
        <html>
            <head><title>Javascript Demo</title></head>
            <body>
            <h1>Javascript callback demo</h1>
            Welcome to this webpage.
            <p>
            If you click the following button, a
            <a href="https://docs.gtk.org/gtk4/class.AlertDialog.html">GtkAlertDialog</a>
            will open:<br>
            <input id="inputId" type="button" value="Open dialog" />
            <p>
            Thanks for visiting.
            </body>
        </html>
        """;
    
    public JavascriptExample(String[] args) {
        app = new Application("io.github.jwharm.javagi.examples.Javascript", ApplicationFlags.DEFAULT_FLAGS);
        app.onActivate(this::activate);
        app.run(args);
    }

    public void activate() {
        try {
            // Create window
            window = new ApplicationWindow(app);
            window.setTitle("WebKit Demo");

            // Create webview
            WebView webview = WebView.builder()
                    .setHeightRequest(300)
                    .setWidthRequest(500)
                    .build();

            // Get the usercontent manager and add the javascript
            var manager = webview.getUserContentManager();
            manager.addScript(new UserScript(
                    script,
                    UserContentInjectedFrames.ALL_FRAMES,
                    UserScriptInjectionTime.END,
                    null,
                    null
            ));

            // Connect the javascript call to a Java callback function
            manager.onScriptMessageReceived(null, this::displayMessage);
            manager.registerScriptMessageHandler("handlerId", null);

            // Load the webview page and display the results
            webview.loadHtml(html, null);
            window.setChild(webview);
            window.present();

        } catch (UnsupportedPlatformException e) {
            System.out.println("Not supported on this platform");
        }
    }

    /*
     * This is the callback method that is triggered from javascript.
     * The Value parameter is a JSCValue, not a GValue.
     */
    private void displayMessage(Value value) {
        if (! value.isObject()) {
            System.out.println("Returned value is expected to be an object");
            return;
        }
        var result = value.objectGetProperty("theMessage");
        AlertDialog dialog = AlertDialog.builder()
                .setMessage("Message received: " + result.toString())
                .build();
        dialog.show(window);
    }
}
