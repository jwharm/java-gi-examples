package io.github.jwharm.javagi.examples.browser;

import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.Bin;
import org.gnome.adw.HeaderBar;
import org.gnome.gio.ApplicationFlags;
import org.gnome.glib.GLib;
import org.gnome.glib.Uri;
import org.gnome.gobject.BindingFlags;
import org.gnome.gtk.*;
import org.gnome.webkit.WebView;

/**
 * This class demonstrates a simple WebKitGtk-based browser application
 */
public class Browser {

    // Home page URL
    private static final String HOME_PAGE = "https://www.gnome.org/";

    private final Application app;

    // When loading=true, the refresh button is changed to a stop button, and vice versa
    private boolean loading = false;

    // Launch the app
    public static void main(String[] args) {
        new Browser(args);
    }

    public Browser(String[] args) {
        app = new Application("io.github.jwharm.javagi.examples.Browser", ApplicationFlags.DEFAULT_FLAGS);
        app.onActivate(this::activate);
        app.run(args);
    }

    public void activate() {
        // Webview component
        WebView webview = new WebView();
        WebView.builder().build();

        // Back button
        var back = Button.builder()
                .iconName("go-previous-symbolic")
                .tooltipText("Back")
                .build();
        back.onClicked(webview::goBack);

        // Forward button
        var forward = Button.builder()
                .iconName("go-next-symbolic")
                .tooltipText("Forward")
                .build();
        forward.onClicked(webview::goForward);

        // Stop / Reload button.
        var stopOrReload = Button.builder()
                .iconName("process-stop-symbolic")
                .tooltipText("Stop")
                .build();
        stopOrReload.onClicked(() -> {
            if (loading) webview.stopLoading();
            else webview.reload();
        });

        // Home button
        var home = Button.builder()
                .iconName("go-home-symbolic")
                .tooltipText("Home")
                .build();
        home.onClicked(() -> webview.loadUri(HOME_PAGE));

        // URL bar
        var urlBar = Entry.builder()
                .inputPurpose(InputPurpose.URL)
                .hexpand(true)
                .build();

        // Container for the webview
        var viewContainer = Bin.builder()
                .vexpand(true)
                .hexpand(true)
                .build();
        viewContainer.setChild(webview);

        // When navigating to another page, update the URL bar
        webview.bindProperty("uri", urlBar.getBuffer(), "text", BindingFlags.DEFAULT);

        // When the webview starts or finishes loading, switch the stop/reload icon and tooltip
        webview.onLoadChanged(event -> {
            switch (event) {
                case STARTED -> {
                    loading = true;
                    stopOrReload.setIconName("process-stop-symbolic");
                    stopOrReload.setTooltipText("Stop");
                }
                case FINISHED -> {
                    loading = false;
                    stopOrReload.setIconName("view-refresh-symbolic");
                    stopOrReload.setTooltipText("Reload");
                }
                default -> {
                    // Ignore all other events
                }
            }
        });

        // When the user navigates to a new URL
        urlBar.onActivate(() -> {
            var url = urlBar.getBuffer().getText();
            var scheme = Uri.peekScheme(url);
            if (scheme == null) {
                url = "https://" + url;
            }
            webview.loadUri(url);
        });

        // Update the progress indicator in the URL bar during loading
        webview.onNotify("estimated-load-progress", $ -> {
            urlBar.setProgressFraction(webview.getEstimatedLoadProgress());
            if (urlBar.getProgressFraction() == 1) {
                GLib.timeoutAddOnce(500, () -> urlBar.setProgressFraction(0));
            }
        });

        // Start loading the home page
        webview.loadUri(HOME_PAGE);

        // Construct the header bar
        var headerbar = new HeaderBar();
        headerbar.packStart(back);
        headerbar.packStart(forward);
        headerbar.packStart(stopOrReload);
        headerbar.packStart(home);
        headerbar.setTitleWidget(urlBar);

        // Pack everything together, and show the window
        var box = new Box(Orientation.VERTICAL, 0);
        box.append(headerbar);
        box.append(viewContainer);

        var window = ApplicationWindow.builder()
                .application(app)
                .heightRequest(700)
                .widthRequest(700)
                .content(box)
                .build();
        window.present();
    }
}
