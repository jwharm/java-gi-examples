import org.javagi.base.GErrorException;
import org.javagi.base.Out;
import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkCallback;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;
import org.gnome.gio.*;
import org.gnome.gio.Settings;
import org.gnome.gobject.BindingFlags;
import org.gnome.gobject.ParamSpec;
import org.gnome.gtk.*;

import java.util.HashSet;
import java.util.Set;

@GtkTemplate(ui="/org/gtk/exampleapp/window.ui")
public class ExampleAppWindow extends ApplicationWindow {

  @GtkChild public Stack stack;
  @GtkChild public MenuButton gears;
  @GtkChild public ToggleButton search;
  @GtkChild public SearchBar searchbar;
  @GtkChild public SearchEntry searchentry;
  @GtkChild public Revealer sidebar;
  @GtkChild public ListBox words;
  @GtkChild public Label lines;
  @GtkChild public Label lines_label;

  private Settings settings;

  public ExampleAppWindow(ExampleApp app) {
    setApplication(app);
  }

  @InstanceInit
  public void init() {
    var builder = GtkBuilder.fromResource("/org/gtk/exampleapp/gears-menu.ui");
    var menu = (MenuModel) builder.getObject("menu");
    gears.setMenuModel(menu);

    settings = new Settings("org.gtk.exampleapp");
    settings.bind("transition", stack, "transition-type", SettingsBindFlags.DEFAULT);
    search.bindProperty("active", searchbar, "search-mode-enabled", BindingFlags.BIDIRECTIONAL);

    settings.bind("show-words", sidebar, "reveal-child", SettingsBindFlags.DEFAULT);
    sidebar.onNotify("reveal-child", _ -> updateWords());
    addAction(settings.createAction("show-words"));

    addAction(new PropertyAction("show-lines", lines, "visible"));
    lines.bindProperty("visible", lines_label, "visible", BindingFlags.DEFAULT);
  }

  public void open(File file) {
    String basename = file.getBasename();

    var scrolled = new ScrolledWindow();
    scrolled.setHexpand(true);
    scrolled.setVexpand(true);
    var view = new TextView();
    view.setEditable(false);
    view.setCursorVisible(false);
    scrolled.setChild(view);
    stack.addTitled(scrolled, basename, basename);
    var buffer = view.getBuffer();

    try {
      var contents = new Out<byte[]>();
      if (file.loadContents(null, contents, null)) {
        String str = new String(contents.get());
        buffer.setText(str, str.length());
      }
    } catch (GErrorException e) {
      throw new RuntimeException(e);
    }

    var tag = buffer.createTag(null, null);
    settings.bind("font", tag, "font", SettingsBindFlags.DEFAULT);
    TextIter startIter = new TextIter();
    TextIter endIter = new TextIter();
    buffer.getStartIter(startIter);
    buffer.getEndIter(endIter);
    buffer.applyTag(tag, startIter, endIter);

    search.setSensitive(true);

    updateWords();
    updateLines();
  }

  @GtkCallback(name="search_text_changed")
  public void searchTextChanged() {
    String text = searchentry.getText();

    if (text.isEmpty())
      return;

    var tab = (ScrolledWindow) stack.getVisibleChild();
    var view = (TextView) tab.getChild();
    var buffer = view.getBuffer();

    // Very simple-minded search implementation
    TextIter startIter = new TextIter();
    TextIter matchStart = new TextIter();
    TextIter matchEnd = new TextIter();
    buffer.getStartIter(startIter);
    if (startIter.forwardSearch(text, TextSearchFlags.CASE_INSENSITIVE,
                                matchStart, matchEnd, null)) {
      buffer.selectRange(matchStart, matchEnd);
      view.scrollToIter(matchStart, 0.0, false, 0.0, 0.0);
    }
  }

  @GtkCallback(name="visible_child_changed")
  public void visibleChildChanged(ParamSpec pspec) {
    if (stack.inDestruction())
      return;

    searchbar.setSearchMode(false);
    updateWords();
    updateLines();
  }

  private void findWord(Button button) {
    String word = button.getLabel();
    searchentry.setText(word);
  }

  private void updateWords() {
    var tab = (ScrolledWindow) stack.getVisibleChild();
    if (tab == null)
      return;

    var view = (TextView) tab.getChild();
    var buffer = view.getBuffer();

    Set<String> strings = new HashSet<>();

    TextIter end, start = new TextIter();
    buffer.getStartIter(start);

    outer:
    while (!start.isEnd()) {
      while (!start.startsWord())
        if (!start.forwardChar())
          break outer;

      end = start.copy();
      if (!end.forwardWordEnd())
        break;

      var word = buffer.getText(start, end, false);
      strings.add(word.toLowerCase());
      start = end.copy();
    }

    Widget child;
    while ((child = words.getFirstChild()) != null)
      words.remove(child);

    for (var key : strings) {
      var row = Button.withLabel(key);
      row.onClicked(() -> findWord(row));
      words.insert(row, -1);
    }
  }

  private void updateLines() {
    var tab = (ScrolledWindow) stack.getVisibleChild();
    if (tab == null)
      return;

    var view = (TextView) tab.getChild();
    var buffer = view.getBuffer();

    int count = buffer.getLineCount();
    lines.setText("%d".formatted(count));
  }
}
