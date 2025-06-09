package io.jwharm.javagi.examples.renderers

import org.gnome.gdk.GLContext
import org.joml.Math.toRadians
import org.joml.Matrix4f
import org.lwjgl.opengles.GLES
import org.lwjgl.opengles.GLES30.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengles.GLES30.GL_FALSE
import org.lwjgl.opengles.GLES30.GL_FLOAT
import org.lwjgl.opengles.GLES30.GL_TRIANGLES
import org.lwjgl.opengles.GLES30.glClear
import org.lwjgl.opengles.GLES30.glClearColor
import org.lwjgl.opengles.GLES30.glDrawArrays
import org.lwjgl.opengles.GLES30.GL_ARRAY_BUFFER
import org.lwjgl.opengles.GLES30.GL_STATIC_DRAW
import org.lwjgl.opengles.GLES30.glBindBuffer
import org.lwjgl.opengles.GLES30.glBufferData
import org.lwjgl.opengles.GLES30.glDeleteBuffers
import org.lwjgl.opengles.GLES30.glGenBuffers
import org.lwjgl.opengles.GLES30.GL_COMPILE_STATUS
import org.lwjgl.opengles.GLES30.GL_FRAGMENT_SHADER
import org.lwjgl.opengles.GLES30.GL_VERTEX_SHADER
import org.lwjgl.opengles.GLES30.glAttachShader
import org.lwjgl.opengles.GLES30.glCompileShader
import org.lwjgl.opengles.GLES30.glCreateProgram
import org.lwjgl.opengles.GLES30.glCreateShader
import org.lwjgl.opengles.GLES30.glDeleteProgram
import org.lwjgl.opengles.GLES30.glEnableVertexAttribArray
import org.lwjgl.opengles.GLES30.glGetShaderInfoLog
import org.lwjgl.opengles.GLES30.glGetShaderi
import org.lwjgl.opengles.GLES30.glGetUniformLocation
import org.lwjgl.opengles.GLES30.glLinkProgram
import org.lwjgl.opengles.GLES30.glShaderSource
import org.lwjgl.opengles.GLES30.glUniformMatrix4fv
import org.lwjgl.opengles.GLES30.glUseProgram
import org.lwjgl.opengles.GLES30.glVertexAttribPointer
import org.lwjgl.opengles.GLES30.glBindVertexArray
import org.lwjgl.opengles.GLES30.glDeleteVertexArrays
import org.lwjgl.opengles.GLES30.glGenVertexArrays

class GLESRenderer : Renderer {

    companion object {

        private val vertexShaderSource = """
            #version 300 es
            layout (location = 0) in vec3 aPos;
            uniform mat4 model;
            uniform mat4 projection;
            void main() {
                gl_Position = projection * model * vec4(aPos.x, aPos.y, aPos.z, 1.0);
            }
        """.trimIndent()
        private val fragmentShaderSource = """
            #version 300 es
            precision mediump float;
            out vec4 FragColor;
            void main() {
                FragColor = vec4(1.0, 0.0, 0.0, 1.0); // Rouge
            }
        """.trimIndent()

    }

    private var vaoId = -1
    private var vboId = -1
    private var shaderProgramId = -1
    private var modelMatrixUniformLocation = -1
    private val modelMatrix = Matrix4f().identity()
    private var projectionMatrixUniformLocation = -1
    private val projectionMatrix = Matrix4f()

    override fun onInit() {
        GLES.createCapabilities()

        // create vertex array object
        vaoId = glGenVertexArrays()
        glBindVertexArray(vaoId)

        // create vertex buffer object
        vboId = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBufferData(GL_ARRAY_BUFFER, triangleVertices, GL_STATIC_DRAW)

        // create and compile shaders
        val vertexShader = createShader(GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = createShader(GL_FRAGMENT_SHADER, fragmentShaderSource)

        // create shader program
        shaderProgramId = glCreateProgram()
        glAttachShader(shaderProgramId, vertexShader!!)
        glAttachShader(shaderProgramId, fragmentShader!!)
        glLinkProgram(shaderProgramId)

        // configure vertices
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)

        // get uniform locations
        modelMatrixUniformLocation = glGetUniformLocation(shaderProgramId, "model")
        projectionMatrixUniformLocation = glGetUniformLocation(shaderProgramId, "projection")

        // set background color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }

    override fun onResize(width: Int, height: Int) {
        // preserve aspect ratio on resize
        val aspectRatio = width.toFloat() / height.toFloat()
        projectionMatrix.identity()
        if (width > height) {
            projectionMatrix.ortho(-aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f)
        } else {
            projectionMatrix.ortho(-1.0f, 1.0f, -1.0f / aspectRatio, 1.0f / aspectRatio, -1.0f, 1.0f)
        }

        glUseProgram(shaderProgramId)
        glUniformMatrix4fv(projectionMatrixUniformLocation, false, projectionMatrix.get(FloatArray(16)))
    }

    override fun onRender(context: GLContext): Boolean {
        glClear(GL_COLOR_BUFFER_BIT)

        // draw triangle
        glUseProgram(shaderProgramId)

        modelMatrix.rotateZ(toRadians(1.0).toFloat())
        glUniformMatrix4fv(modelMatrixUniformLocation, false, modelMatrix.get(FloatArray(16)))

        glBindVertexArray(vaoId)
        glDrawArrays(GL_TRIANGLES, 0, 3)

        return true
    }

    override fun onDestroy() {
        // free resources
        glDeleteVertexArrays(vaoId)
        glDeleteBuffers(vboId)
        glDeleteProgram(shaderProgramId)
    }

    private fun createShader(type: Int, source: String): Int? {
        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            val infoLog = glGetShaderInfoLog(shader)
            println("Shader compilation error: $infoLog")
            return null
        }

        return shader
    }

}