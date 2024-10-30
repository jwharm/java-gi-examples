package io.github.jwharm.javagi.examples.listviewer;

import io.github.jwharm.javagi.gio.ListIndexModel;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gtk.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ListViewer extends Application {

    private final List<String> list;
    private final ListIndexModel listIndexModel;
    private final Random rnd = new Random();

    public void activate() {
        var window = new ApplicationWindow(this);
        window.setTitle("ListViewer example");
        window.setDefaultSize(300, 500);

        var box = new Box(Orientation.VERTICAL, 0);

        SignalListItemFactory factory = new SignalListItemFactory();
        factory.onSetup(object -> {
            ListItem listitem = (ListItem) object;
            listitem.setChild(new Label(""));
        });
        factory.onBind(object -> {
            ListItem listitem = (ListItem) object;
            Label label = (Label) listitem.getChild();
            ListIndexModel.ListIndex item = (ListIndexModel.ListIndex) listitem.getItem();
            if (label == null || item == null)
                return;

            // The ListIndexModel contains ListIndexItems that contain only their index in the list.
            int index = item.getIndex();
            
            // Retrieve the index of the item and show the entry from the ArrayList with random strings.
            String text = list.get(index);
            label.setLabel(text);
        });

        ScrolledWindow scroll = new ScrolledWindow();
        ListView lv = new ListView(new SingleSelection<>(listIndexModel), factory);
        scroll.setChild(lv);
        scroll.setVexpand(true);
        box.append(scroll);

        window.setChild(box);
        window.present();
    }

    // Generate a short random string
    private String randomString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = rnd.nextInt(5, 10); i < len; i++) {
            sb.append((char) rnd.nextInt('a', 'z' + 1));
        }
        return sb.toString();
    }

    public ListViewer(String[] args) {
        super("io.github.jwharm.javagi.example.ListView", ApplicationFlags.FLAGS_NONE);

        // Build a list with many (between 500 and 1000) random strings.
        // The list is a normal java ArrayList<String>, nothing special.
        list = new ArrayList<>();
        for (int i = 0, len = rnd.nextInt(500, 1000); i < len; i++) {
            list.add(randomString());
        }
        listIndexModel = ListIndexModel.newInstance(list.size());

        onActivate(this::activate);
        run(args);
    }
    
    public static void main(String[] args) {
        new ListViewer(args);
    }
}

