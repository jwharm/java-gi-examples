package io.jwharm.javagi.examples.renderers

import org.gnome.gdk.GLContext

val triangleVertices = floatArrayOf(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    0.0f, 0.5f, 0.0f,
)

interface Renderer {

    fun onInit()
    fun onResize(width: Int, height: Int)
    fun onRender(context: GLContext): Boolean
    fun onDestroy()

}