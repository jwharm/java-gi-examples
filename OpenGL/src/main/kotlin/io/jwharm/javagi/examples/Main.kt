package io.jwharm.javagi.examples

import io.jwharm.javagi.examples.renderers.GLESRenderer
import io.jwharm.javagi.examples.renderers.GLRenderer
import io.jwharm.javagi.examples.renderers.Renderer
import org.gnome.adw.Application
import org.gnome.adw.ApplicationWindow
import org.gnome.adw.ToolbarView
import org.gnome.gdk.GLAPI
import org.gnome.gio.ApplicationFlags
import org.gnome.gtk.*

fun main(args: Array<String>) {
    App().run(args)
}

class App: Application("io.github.jwharm.javagi.examples.OpenGL", ApplicationFlags.DEFAULT_FLAGS) {
    init {
        onActivate {
            Window(this).present()
        }
    }
}

class Window(
    application: Application,
): ApplicationWindow(application) {

    private lateinit var renderer: Renderer
    private var tickCallback: Int = -1

    init {
        title = "OpenGL"
        setDefaultSize(250, 300)
        content = ToolbarView().apply {
            addTopBar(HeaderBar())
            content = GLArea().apply { setup() }
        }
    }

    private fun GLArea.setup() {
        hexpand  = true
        vexpand = true
        setSizeRequest(500, 500)
        // You could force the use of a certain API as follows (if it's supported)
        // allowedApis = mutableSetOf(GLAPI.GL)
        // allowedApis = mutableSetOf(GLAPI.GLES)

        onRealize {
            makeCurrent()
            println("Graphics API: $api")

            // select a renderer for the current API
            renderer = when (api.first()) {
                GLAPI.GL -> GLRenderer()
                GLAPI.GLES -> GLESRenderer()
            }

            renderer.onInit()

            // for continuous rendering
            tickCallback = addTickCallback { _, _ ->
                queueRender()
                true
            }
        }

        onUnrealize {
            removeTickCallback(tickCallback)

            renderer.onDestroy()
        }

        onResize { width, height -> renderer.onResize(width, height) }
        onRender { context -> renderer.onRender(context) }
    }
}
