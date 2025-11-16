package io.github.jwharm.javagi.examples.pegsolitaire;

import org.javagi.base.Out;
import org.javagi.gobject.types.Types;
import org.gnome.gdk.*;
import org.gnome.gdk.Snapshot;
import org.gnome.gio.ApplicationFlags;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gobject.Value;
import org.gnome.graphene.Rect;
import org.gnome.gtk.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Set;

/* Peg Solitaire
 * #Keywords: GtkGridView, game, drag-and-drop, dnd
 *
 * This demo demonstrates how to use drag-and-drop to implement peg solitaire.
 *
 */
public class PegSolitaire {

    private static CssProvider provider;

    /* Create an object for the pegs that get moved around in the game.
     *
     * We implement the GdkPaintable interface for them, so we can use GtkPicture
     * objects for the wholes we put the pegs into.
     */
    public static class SolitairePeg extends GObject implements Paintable {

        public int x;
        public int y;

        // Explicitly register the GType, because it is used later on
        public static Type gtype = Types.register(SolitairePeg.class);

        public SolitairePeg(MemorySegment address) {
            super(address);
        }

        // Here, we implement the functionality required by the GdkPaintable interface

        // The snapshot function is the only function we need to implement.
        // It does the actual drawing of the paintable.
        @Override
        public void snapshot(Snapshot snapshot, double width, double height) {
            try (Arena arena = Arena.ofConfined()) {
                new org.gnome.gtk.Snapshot(snapshot.handle()).appendColor(
                        new RGBA(0.6F, 0.3F, 0.0F, 1.0F, arena),
                        Rect.alloc().init(0, 0, (float) width, (float) height)
                );
            }
        }

        /* The flags are very useful to let GTK know that this image
         * is never going to change.
         * This allows many optimizations and should therefore always
         * be set.
         */
        @Override
        public Set<PaintableFlags> getFlags() {
            return Set.of(PaintableFlags.CONTENTS, PaintableFlags.SIZE);
        }

        @Override
        public int getIntrinsicWidth() {
            return 32;
        }

        @Override
        public int getIntrinsicHeight() {
            return 32;
        }

        /* Add a little setter for the peg's position.
         * We want to track those so that we can check for legal moves
         * during drag-and-drop operations.
         */
        void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        // And finally, we add a simple constructor.
        public SolitairePeg() {
        }
    }

    /*** Helper for finding a win ***/

    private void celebrate(boolean win) {
        String path;

        if (win)
            path = GLib.buildFilename(
                    "usr", "share", "sounds", "freedesktop", "stereo", "complete.oga", null);
        else
            path = GLib.buildFilename(
                    "usr", "share", "sounds", "freedesktop", "stereo", "suspend-error.oga", null);
        MediaStream stream = MediaFile.forFilename(path);
        stream.setVolume(1.0);
        stream.play();
    }

    private int checkMove(Grid grid, int x, int y, int dx, int dy) {
        // We have a peg at x, y.
        // Check if we can move the peg to x + 2*dx, y + 2*dy
        Widget widget = grid.getChildAt(x + dx, y + dy);
        if ((! (widget instanceof Image image)) ||
                (! (image.getPaintable() instanceof SolitairePeg)))
            return 0;

        widget = grid.getChildAt(x + 2*dx, y + 2*dy);
        if ((! (widget instanceof Image image_)) ||
                (! (image_.getPaintable() instanceof SolitairePeg)))
            return 0;

        return 1;
    }

    private void checkForEnd(Grid grid) {
        int pegs = 0;
        int moves = 0;

        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                Widget widget = grid.getChildAt(x, y);
                if (widget instanceof Image image && image.getPaintable() instanceof SolitairePeg) {
                    pegs++;
                    moves += checkMove(grid, x, y, 1, 0);
                    moves += checkMove(grid, x, y, -1, 0);
                    moves += checkMove(grid, x, y, 0, 1);
                    moves += checkMove(grid, x, y, 0, -1);
                }
                if (pegs > 1 && moves > 0)
                    break;
            }
        }

        Widget widget = grid.getChildAt(3, 3);
        if (pegs == 1 && widget instanceof Image image && image.getPaintable() instanceof SolitairePeg) {
            celebrate(true);
        } else if (moves == 0) {
            celebrate(false);
        }
    }

    /*** DRAG AND DROP ***/

    /* The user tries to start a drag operation.
     * We check if the image contains a peg, and if so, we return the
     * peg as the content to be dragged.
     */
    private ContentProvider dragPrepare(Image image) {
        var paintable = image.getPaintable();
        if (! (paintable instanceof SolitairePeg))
            return null;

        return ContentProvider.typed(SolitairePeg.gtype, paintable);
    }

    /* This notifies us that the drag has begun.
     * We can now set up the icon and the widget for the ongoing drag.
     */
    private void dragBegin(DragSource source, Drag drag, Image image) {
        var paintable = image.getPaintable();

        /* We guaranteed in the drag_prepare function above that we
         * only start a drag if a peg is available.
         * So let's make sure we did not screw that up.
         */
        var peg = (SolitairePeg) paintable;

        // We use the peg as the drag icon.
        source.setIcon(peg, -2, -2);

        // We also attach it to the drag operation as custom user data,
        // so that we can get it back later if the drag fails.
        drag.setData("the peg", peg.handle());

        // Because we are busy dragging the peg, we want to unset it
        // on the image.
        image.clear();
    }

    /* This is called once a drag operation has ended (successfully or not).
     * We want to undo what we did in drag_begin() above and react
     * to a potential move of the peg.
     */
    private void dragEnd(Drag drag, boolean deleteData, Image image) {
        /* If the drag was successful, we should now delete the peg.
         * We did this in drag_begin() above to prepare for the drag, so
         * there's no need to do anything anymore.
         */
        if (deleteData)
            return;

        /* However, if the drag did not succeed, we need to undo what
         * we did in drag_begin() and reinsert the peg here.
         * Because we used it as the drag data
         */
        var peg = new SolitairePeg(drag.getData("the peg"));
        image.setFromPaintable(peg);
    }

    /* Whenever a new drop operation starts, we need to check if we can
     * accept it.
     * The default check unfortunately is not good enough, because it only
     * checks the data type. But we also need to check if our image can
     * even accept data.
     */
    boolean dropAccept(Drop drop, Image image) {
        // First, check the drop is actually trying to drop a peg
        if (! drop.getFormats().containGtype(SolitairePeg.gtype))
            return false;

        // If the image already contains a peg, we cannot accept another one
        return !(image.getPaintable() instanceof SolitairePeg);
    }

    private boolean dropDrop(Value value, Image image) {
        var grid = (Grid) image.getParent();

        // The value contains the data in the type we demanded.
        // We demanded a SolitairePeg, so that's what we get.
        var peg = (SolitairePeg) value.getObject();

        // Make sure this was a legal move.
        // First, figure out the image's position in the grid.
        Out<Integer> _imageX = new Out<>();
        Out<Integer> _imageY = new Out<>();
        grid.queryChild(image, _imageX, _imageY, null, null);
        int imageX = _imageX.get();
        int imageY = _imageY.get();

        // If the peg was not moved 2 spaces horizontally or vertically,
        // this was not a valid jump. Reject it.
        if (! ((Math.abs(imageX - peg.x) == 2 && imageY == peg.y) ||
                (Math.abs(imageY - peg.y) == 2 && imageX == peg.x)))
            return false;

        // Get the widget that was jumped over
        var jumped = (Image) grid.getChildAt((imageX + peg.x) / 2, (imageY + peg.y) / 2);

        // If the jumped widget does not have a peg in it, this move
        // isn't valid.
        if (! (jumped.getPaintable() instanceof SolitairePeg))
            return false;

        // Finally, we know it's a legal move.

        // Clear the peg of the jumped-over image
        jumped.clear();

        // Add the peg to this image
        peg.setPosition(imageX, imageY);
        image.setFromPaintable(peg);

        // Maybe we have something to celebrate
        checkForEnd(grid);

        // Success!
        return true;
    }

    private void createBoard(Window window) {
        String css =
                ".solitaire-field {" +
                "  border: 1px solid lightgray;" +
                "}";

        provider = new CssProvider();
        provider.loadFromString(css);
        Gtk.styleContextAddProviderForDisplay(Display.getDefault(), provider, 800);

        var grid = Grid.builder()
                .setHalign(Align.CENTER)
                .setValign(Align.CENTER)
                .setRowSpacing(6)
                .setColumnSpacing(6)
                .setRowHomogeneous(true)
                .setColumnHomogeneous(true)
                .build();
        window.setChild(grid);

        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                if ((x < 2 || x >= 5) && (y < 2 || y >= 5))
                    continue;

                var image = new Image();
                image.addCssClass("solitaire-field");
                image.setIconSize(IconSize.LARGE);
                if (x != 3 || y != 3) {
                    var peg = new SolitairePeg();
                    peg.setPosition(x, y);
                    image.setFromPaintable(peg);
                }

                grid.attach(image, x, y, 1, 1);

                /* Set up the drag source.
                 * This is rather straightforward: Set the supported actions
                 * (in our case, pegs can only be moved) and connect all the
                 * relevant signals.
                 * And because all drag'n'drop handling is done via event controllers,
                 * we need to add the controller to the widget.
                 */
                var source = new DragSource();
                source.setActions(DragAction.MOVE);
                source.onPrepare((_, _) -> dragPrepare(image));
                source.onDragBegin(drag -> dragBegin(source, drag, image));
                source.onDragEnd((drag, deleteData) -> dragEnd(drag, deleteData, image));
                image.addController(source);

                // Set up the drop target.
                // This is more involved, because the game logic goes here.

                // First we specify the data we accept: pegs.
                // And we only want moves.
                var target = new DropTarget(SolitairePeg.gtype, DragAction.MOVE);
                // Then we connect our signals.
                target.onAccept(drop -> dropAccept(drop, image));
                target.onDrop((value, _, _) -> dropDrop(value, image));
                // Finally, like above, we add it to the widget.
                image.addController(target);
            }
        }
    }

    public PegSolitaire(Application application) {
        var header = new HeaderBar();
        var window = ApplicationWindow.builder()
                .setApplication(application)
                .setTitle("Peg Solitaire")
                .setTitlebar(header)
                .setDefaultWidth(400).setDefaultHeight(400)
                .build();

        var restart = Button.fromIconName("view-refresh-symbolic");
        restart.onClicked(() -> createBoard(window));
        header.packStart(restart);

        createBoard(window);
        window.setVisible(true);
    }

    public static void main(String[] args) {
        var app = new Application("io.github.jwharm.javagi.examples.PegSolitaire", ApplicationFlags.DEFAULT_FLAGS);
        app.onActivate(() -> new PegSolitaire(app));
        app.run(args);

        Gtk.styleContextRemoveProviderForDisplay(Display.getDefault(), PegSolitaire.provider);
    }
}
