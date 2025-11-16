package io.github.jwharm.javagi.examples.columnview;

import org.javagi.gobject.types.Types;
import org.gnome.gio.ApplicationFlags;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.*;
import org.gnome.gio.ListStore;

/**
 * Example class for constructing a Gtk ColumnView with a lengthy table of data.
 */
public class ColumnViewDatagrid {
    
    Application app;
    ListStore<Row> store;

    public ColumnViewDatagrid(String[] args) {
        app = new Application("my.example.ColumnView", ApplicationFlags.DEFAULT_FLAGS);
        app.onActivate(this::onActivate);
        app.run(args);
    }

    private void onActivate() {
        /*
         * The ListStore is the data model behind our Gtk ColumnView. It is
         * populated with Row instances. The Row class is defined below. For
         * this example it simply contains two Strings, one for each column.
         */
        store = new ListStore<>(Row.gtype);
        for (int i = 0; i < 1000; i++)
            store.append(new Row("col1 " + i, "col2 " + i));

        // Create the ColumnView and put it in a scrollable window
        var sel = new SingleSelection<Row>(store);
        var columnview = createColumnView(sel);
        var scroll = ScrolledWindow.builder()
                // Disable horizontal scrolling
                .setHscrollbarPolicy(PolicyType.NEVER)
                .build();
        scroll.setChild(columnview);

        // Create and present the window
        var window = ApplicationWindow.builder()
                .setDefaultWidth(320)
                .setDefaultHeight(480)
                .setApplication(app)
                .setTitle("ColumnView Example")
                .build();
        window.setChild(scroll);
        window.present();
    }

    private static ColumnView createColumnView(SingleSelection<Row> sel) {
        var columnview = new ColumnView(sel);

        // One ListItemFactory for each column
        var col1factory = new SignalListItemFactory();
        var col2factory = new SignalListItemFactory();

        /*
         * A SignalListItemFactory creates and binds widgets for the list items.
         * We use a Gtk Inscription widget to display the text.
         */
        col1factory.onSetup(item -> {
            var listitem = (ListItem) item;
            var inscription = Inscription.builder()
                    .setXalign(0)
                    .build();
            listitem.setChild(inscription);
        });
        col1factory.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();
            inscription.setText(row.col1);
        });

        col2factory.onSetup(item -> {
            var listitem = (ListItem) item;
            var inscription = Inscription.builder()
                    .setXalign(0)
                    .build();
            listitem.setChild(inscription);
        });
        col2factory.onBind(item -> {
            var listitem = (ListItem) item;
            var inscription = (Inscription) listitem.getChild();
            var row = (Row) listitem.getItem();
            inscription.setText(row.col2);
        });

        // Create the columns and add them to the column view
        var col1 = new ColumnViewColumn("Column 1", col1factory);
        var col2 = new ColumnViewColumn("Column 2", col2factory);
        columnview.appendColumn(col1);
        columnview.appendColumn(col2);
        return columnview;
    }

    /**
     * This class represents one row of data. To use it in a ListStore, it must
     * be registered as a GObject-derived type. Other than that, it's just a
     * plain Java class.
     */
    public static final class Row extends GObject {
        public static Type gtype = Types.register(Row.class);
        public String col1;
        public String col2;
        
        public Row(String col1, String col2) {
            this.col1 = col1;
            this.col2 = col2;
        }
    }

    public static void main(String[] args) {
        new ColumnViewDatagrid(args);
    }
}
