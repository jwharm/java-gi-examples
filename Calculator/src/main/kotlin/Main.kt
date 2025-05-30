package io.jwharm.javagi.examples

import org.gnome.adw.Application
import org.gnome.adw.ApplicationWindow
import org.gnome.gdk.Display
import org.gnome.gdk.Gdk
import org.gnome.gtk.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    CalculatorApplication().run(args)
}

class CalculatorApplication : Application() {
    init {
        applicationId = "my.example.Calculator"

        onActivate {
            val provider = CssProvider()
            provider.loadFromPath("src/main/resources/Calculator.css")
            Gtk.styleContextAddProviderForDisplay(
                Display.getDefault(),
                provider,
                Gtk.STYLE_PROVIDER_PRIORITY_APPLICATION
            )

            CalculatorWindow(this).present()
        }
    }
}

class CalculatorWindow(app: Application) : ApplicationWindow() {
    private var clean = false
    private var function: ((Double, Double) -> Double)? = null
    private var accumulator = 0.0
    private val buttonResult = createFunctionButton('=').apply {
        addCssClass("suggested-action")
    }

    private val entry: Entry = Entry().apply {
        placeholderText = "0.0"
        editable = false
        alignment = 1f // right-align
        addCssClass("monospace")
    }

    private val entryValue: Double
        get() = if (entry.text.isEmpty()) 0.0 else entry.text.toDouble()

    init {
        application = app
        title = "Calculator"
        setDefaultSize(250, 300)
        addController(EventControllerKey().apply {
            onKeyPressed { keyval, _, _ -> keyPressed(keyval) }
        })

        content = Grid().apply {
            columnSpacing = 1
            rowSpacing = 1

            attach(HeaderBar().apply {
                hexpand = true
                packStart(Button.withLabel("AC").apply {
                    addCssClass("destructive-action")
                    onClicked(this@CalculatorWindow::clear)
                })
            }, 0, 0, 4, 1)

            attach(entry, 0, 1, 4, 1)

            // Create the digit buttons and place them in the grid
            attach(createInputButton('7'), 0, 2, 1, 1)
            attach(createInputButton('8'), 1, 2, 1, 1)
            attach(createInputButton('9'), 2, 2, 1, 1)
            attach(createInputButton('4'), 0, 3, 1, 1)
            attach(createInputButton('5'), 1, 3, 1, 1)
            attach(createInputButton('6'), 2, 3, 1, 1)
            attach(createInputButton('1'), 0, 4, 1, 1)
            attach(createInputButton('2'), 1, 4, 1, 1)
            attach(createInputButton('3'), 2, 4, 1, 1)
            attach(createInputButton('0'), 0, 5, 1, 1)
            attach(createInputButton('.'), 1, 5, 1, 1)

            // Create the function buttons and place them in the grid
            attach(createFunctionButton('*'), 3, 2, 1, 1)
            attach(createFunctionButton('/'), 3, 3, 1, 1)
            attach(createFunctionButton('+'), 3, 4, 1, 1)
            attach(createFunctionButton('-'), 3, 5, 1, 1)
            attach(buttonResult.also {
                grabFocus()
            }, 2, 5, 1, 1)
        }
    }

    private fun createInputButton(label: Char): Button = Button.withLabel(label.toString()).apply {
        vexpand = true
        hexpand = true
        onClicked {
            input(label)
        }
    }

    private fun createFunctionButton(label: Char): Button = Button.withLabel(label.toString()).apply {
        vexpand = true
        hexpand = true
        onClicked {
            setFunction(label)
        }
    }

    private fun keyPressed(keyval: Int): Boolean {
        val key = Gdk.keyvalName(keyval)
        when(key) {
            "BackSpace" -> backspace()
            "equal" -> setFunction('=')
            "plus" -> setFunction('+')
            "minus" -> setFunction('-')
            "asterisk" -> setFunction('*')
            "slash" -> setFunction('/')
            "period", "comma" -> input('.')
            "Escape" -> exitProcess(0)
            "c" -> clear()
            in "0".."9" -> input(key[0])
        }

        return true
    }

    private fun backspace() = with(entry.text) {
        if (this.isNotEmpty())
            entry.text = this.substring(0, this.length - 1)
    }

    private fun input(input: Char) {
        if (clean) {
            accumulator = entryValue
            entry.text = ""
            clean = false
        }

        if (input.isDigit() || (input == '.' && !entry.text.contains('.')))
            entry.text += input

        buttonResult.grabFocus()
    }

    private fun setFunction(input: Char) {
        calculate()
        buttonResult.grabFocus()
        function = when(input) {
            '+' -> { x, y -> x + y }
            '-' -> { x, y -> x - y }
            '*' -> { x, y -> x * y }
            '/' -> { x, y -> x / y }
            '=' -> null
            else -> function
        }
    }

    private fun calculate() {
        clean = true
        function?.let {
            entry.text = it(accumulator, entryValue).toString().replace("\\.0$", "")
        }
    }

    private fun clear() {
        entry.text = ""
        accumulator = 0.0
        clean = false
        function = null
    }
}
