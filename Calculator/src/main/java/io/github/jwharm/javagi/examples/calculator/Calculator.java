package io.github.jwharm.javagi.examples.calculator;

import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.Application;
import org.gnome.adw.HeaderBar;
import org.gnome.gdk.Display;
import org.gnome.gdk.Gdk;
import org.gnome.gdk.ModifierType;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gtk.*;

import java.util.Set;
import java.util.function.DoubleBinaryOperator;

/**
 * A small example calculator app
 */
public class Calculator extends Application {

    private Entry display;
    private Button buttonResult;
    private DoubleBinaryOperator function; // operator on two doubles
    private double accumulator = 0;
    private boolean clean = false;

    public static void main(String[] args) {
        var app = new Calculator("io.github.jwharm.samples.Calculator", ApplicationFlags.NON_UNIQUE);
        app.onActivate(app::activate);
        app.run(args);
    }
    
    public Calculator(String applicationId, ApplicationFlags flags) {
        super(applicationId, flags);
    }
    
    // Setup the window and add a key-press event controller
    public void activate() {
        loadCSS();
        var window = new ApplicationWindow(this);
        window.setDefaultSize(250, 300);
        window.setTitle("Calculator");

        // Add event controller for key presses
        var controller = new EventControllerKey();
        controller.onKeyPressed(this::keyPressed);
        window.addController(controller);
        
        setupWidgets(window);
    }

    // We don't really use CSS styling in this app, but 
    // it's nice to see that it works
    private void loadCSS() {
        // Instantiate style provider from CSS file
        var provider = new CssProvider();
        provider.loadFromPath("src/main/resources/Calculator.css");
        
        // Add the provider to the display
        StyleContext.addProviderForDisplay(
                Display.getDefault(),
                provider, 
                Gtk.STYLE_PROVIDER_PRIORITY_APPLICATION
        );
    }

    // Setup the header bar, display entry, and buttons
    private void setupWidgets(ApplicationWindow window) {
        var grid = new Grid();
        grid.setColumnSpacing(1);
        grid.setRowSpacing(1);
        
        var headerbar = new HeaderBar();
        headerbar.setHexpand(true);
        grid.attach(headerbar, 0, 0, 4, 1);
        
        var buttonClear = Button.withLabel("AC");
        buttonClear.addCssClass("destructive-action");
        headerbar.packStart(buttonClear);
        buttonClear.onClicked(this::clear);
        
        display = new Entry();
        display.setAlignment(1f);
        display.setPlaceholderText("0");
        display.setEditable(false);
        display.addCssClass("monospace");

        grid.attach(display, 0, 1, 4, 1);
        
        // The input buttons are created in a helper function (defined further below)
        grid.attach(createInputButton('7'), 0, 2, 1, 1);
        grid.attach(createInputButton('8'), 1, 2, 1, 1);
        grid.attach(createInputButton('9'), 2, 2, 1, 1);
        grid.attach(createInputButton('4'), 0, 3, 1, 1);
        grid.attach(createInputButton('5'), 1, 3, 1, 1);
        grid.attach(createInputButton('6'), 2, 3, 1, 1);
        grid.attach(createInputButton('1'), 0, 4, 1, 1);
        grid.attach(createInputButton('2'), 1, 4, 1, 1);
        grid.attach(createInputButton('3'), 2, 4, 1, 1);
        grid.attach(createInputButton('0'), 0, 5, 1, 1);
        grid.attach(createInputButton('.'), 1, 5, 1, 1);
        
        buttonResult = createFunctionButton('=');
        buttonResult.addCssClass("suggested-action");
        grid.attach(buttonResult, 2, 5, 1, 1);
        
        grid.attach(createFunctionButton('*'), 3, 2, 1, 1);
        grid.attach(createFunctionButton('/'), 3, 3, 1, 1);
        grid.attach(createFunctionButton('+'), 3, 4, 1, 1);
        grid.attach(createFunctionButton('-'), 3, 5, 1, 1);
        
        window.setContent(grid);
        window.present();

        // Make sure that the '=' button has the keyboard focus.
        // Pressing <Enter> should always activate the '=' button.
        buttonResult.grabFocus();
    }
    
    // Small helper function to create keypad buttons and attach a signal
    private Button createInputButton(char label) {
        var button = Button.withLabel(String.valueOf(label));
        button.setVexpand(true);
        button.onClicked(() -> input(label));
        return button;
    }
    
    // Small helper function to create function buttons and attach a signal
    private Button createFunctionButton(char label) {
        var button = Button.withLabel(String.valueOf(label));
        button.onClicked(() -> setFunction(label));
        return button;
    }
    
    // Handle key press events
    public boolean keyPressed(int keyval, int keycode, Set<ModifierType> state) {
        String key = Gdk.keyvalName(keyval);
        if (key != null)
            switch (key) {
                case "BackSpace" -> backspace();
                case "equal" -> setFunction('=');
                case "plus" -> setFunction('+');
                case "minus" -> setFunction('-');
                case "asterisk" -> setFunction('*');
                case "slash" -> setFunction('/');
                case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> input(key.charAt(0));
                case "period", "comma" -> input('.');
                case "c" -> clear();
                case "Escape" -> quit();
            }
        buttonResult.grabFocus();
        return true;
    }
    
    // Remove the rightmost digit from the display entry
    private void backspace() {
        int length = display.getText().length();
        if (length > 0) {
            display.setText(display.getText().substring(0, length - 1));
        }
    }
    
    // Input a digit or a decimal separator
    private void input(char input) {
        // If the user is starting to type a new number, save the previous
        // number into the accumulator.
        if (clean) {
            accumulator = getDisplayValue();
            display.setText("");
            clean = false;
        }
        if (input == '.') {
            if (! display.getText().contains(".")) {
                display.setText(display.getText() + input);
            }
        } else if (Character.isDigit(input)) {
            display.setText(display.getText() + input);
        }
        buttonResult.grabFocus();
    }
    
    // Trigger calculation and set the new function to be 
    // executed the next time ('*', '/', '+', '-')
    private void setFunction(char input) {
       calculate();
       function = switch (input) {
            case '*' -> (double a, double b) -> a * b;
            case '/' -> (double a, double b) -> a / b;
            case '+' -> (double a, double b) -> a + b;
            case '-' -> (double a, double b) -> a - b;
            case '=' -> null;
            default -> function;
        };
        buttonResult.grabFocus();
    }
    
    // Calculate the result and display it on screen
    private void calculate() {
        double in = getDisplayValue();
        if (function != null) {
            // Run the calculation function
            double out = function.applyAsDouble(accumulator, in);
            // Remove ".0" and display the result
            display.setText(Double.toString(out).replaceAll("\\.0$", ""));
        }
        clean = true;
    }
    
    // Clear the display and the action status
    private void clear() {
        display.setText("");
        accumulator = 0;
        clean = false;
        function = null;
    }
    
    // Get the currently displayed value, or 0 if it's empty
    private double getDisplayValue() {
        String text = display.getText();
        return (text.isEmpty()) ? 0 : Double.parseDouble(text);
    }
}
