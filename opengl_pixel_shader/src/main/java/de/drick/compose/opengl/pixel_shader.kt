package de.drick.compose.opengl

import android.opengl.GLES20
import de.drick.common.log
import de.drick.compose.opengl.GLRenderer
import org.intellij.lang.annotations.Language
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.opengles.GL10


@Language("GLSL")
val simpleFragmentShader = """
    precision mediump float;
    
    varying vec2  fragCoord;   // pixel coordinates
    uniform vec2  iResolution; // viewport resolution (in pixels)
    
    void main() {
        // Normalized pixel coordinates (from 0 to 1)
        vec2 uv = fragCoord/iResolution.xy;
    
        // Time varying pixel color
        vec3 col = 0.5 + 0.5*cos(uv.xyx+vec3(0,2,4));
    
        // Output to screen
        gl_FragColor = vec4(col,1.0);
    }
""".trimIndent()

interface UniformDSL {
    fun setFloatUniform(name: String, value1: Float) {
        setFloatUniform(name, arrayOf(value1))
    }
    fun setFloatUniform(name: String, value1: Float, value2: Float) {
        setFloatUniform(name, arrayOf(value1, value2))
    }
    fun setFloatUniform(name: String, value1: Float, value2: Float, value3: Float) {
        setFloatUniform(name, arrayOf(value1, value2, value3))
    }
    fun setFloatUniform(name: String, values: Array<Float>)

    fun setIntUniform(name: String, value1: Int) {
        setIntUniform(name, arrayOf(value1))
    }
    fun setIntUniform(name: String, values: Array<Int>)
}

class PixelShader(
    private val fragmentShaderSrc: String = simpleFragmentShader
) {
    @Language("GLSL")
    val vertexSource = """
    attribute vec4 vPosition;
    varying vec2 fragCoord;
    void main() {
       gl_Position = vPosition;
       vec2 texPos = vec2(vPosition.x, vPosition.y) / 2.0 + 0.5;
       fragCoord = texPos;
    }
    """

    private val positionComponentCount = 2

    private val quadVertices by lazy {
        floatArrayOf(
            -1f, 1f,
            1f, 1f,
            -1f, -1f,
            1f, -1f
        )
    }

    private val bytesPerFloat = 4

    private val verticesData by lazy {
        ByteBuffer.allocateDirect(quadVertices.size * bytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().also {
                it.put(quadVertices)
            }
    }

    private val uniforms = mutableSetOf<String>()
    private val uniformLocations = mutableMapOf<String, Int>()
    private val uniformIntValues = mutableMapOf<String, Array<Int>>()
    private val uniformFloatValues = mutableMapOf<String, Array<Float>>()

    private fun registerUniform(name: String) {
        uniforms.add(name)
    }

    fun setIntUniform(name: String, value1: Int) {
        setIntUniform(name, arrayOf(value1))
    }
    private fun setIntUniform(name: String, values: Array<Int>) {
        registerUniform(name)
        uniformIntValues[name] = values
        renderer.requestRender()
    }
    private fun setFloatUniform(name: String, values: Array<Float>) {
        registerUniform(name)
        uniformFloatValues[name] = values
        renderer.requestRender()
    }

    val renderer = GLRenderer {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glDisable(GL10.GL_DITHER)
        GLES20.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)

        val programId = GLES20.glCreateProgram()
        require(programId > 0) { "Unable to create program!" }
        val fragShader = createAndVerifyShader(fragmentShaderSrc, GLES20.GL_FRAGMENT_SHADER)
        val vertShader = createAndVerifyShader(vertexSource, GLES20.GL_VERTEX_SHADER)
        GLES20.glAttachShader(programId, fragShader)
        GLES20.glAttachShader(programId, vertShader)
        GLES20.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val msg = "Linking of program failed. ${GLES20.glGetProgramInfoLog(programId)}"
            GLES20.glDeleteProgram(programId)
            throw IllegalStateException(msg)
        }

        if (validateProgram(programId).not()) throw IllegalStateException("Program validation failed!")

        val attributeLocation = GLES20.glGetAttribLocation(programId, "vPosition")
        uniforms.forEach { name -> // get location for uniforms
            val loc = GLES20.glGetUniformLocation(programId, name)
            uniformLocations[name] = loc
        }

        val resolutionUniform = GLES20.glGetUniformLocation(programId, "iResolution")

        verticesData.position(0)
        GLES20.glVertexAttribPointer(
            attributeLocation,
            positionComponentCount,
            GLES20.GL_FLOAT,
            false,
            0,
            verticesData
        )

        GLES20.glDetachShader(programId, vertShader)
        GLES20.glDetachShader(programId, fragShader)
        GLES20.glDeleteShader(vertShader)
        GLES20.glDeleteShader(fragShader)

        var surfaceWidth = 0f
        var surfaceHeight = 0f

        onSurfaceChanged { width, height ->
            GLES20.glViewport(0, 0, width, height)
            surfaceWidth = width.toFloat()
            surfaceHeight = height.toFloat()
        }

        var frameCount = 0
        onDrawFrame {
            GLES20.glDisable(GL10.GL_DITHER)
            GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT)

            GLES20.glUseProgram(programId)
            GLES20.glUniform2f(resolutionUniform, surfaceWidth, surfaceHeight)

            //set int uniforms
            uniformIntValues.forEach { (name, values) ->
                uniformLocations[name]?.let { loc ->
                    //println("setUniform($name, ${values.joinToString()}) pos: $loc")
                    when (values.size) {
                        1 -> GLES20.glUniform1i(loc, values[0])
                        2 -> GLES20.glUniform2i(loc, values[0], values[1])
                        3 -> GLES20.glUniform3i(loc, values[0], values[1], values[2])
                        4 -> GLES20.glUniform4i(loc, values[0], values[1], values[2], values[3])
                        else -> throw IllegalStateException("Wrong number of values! : ${values.size}")
                    }
                }
            }
            uniformFloatValues.forEach { (name, values) ->
                uniformLocations[name]?.let { loc ->
                    //println("setUniform($name, ${values.joinToString()}) pos: $loc")
                    when (values.size) {
                        1 -> GLES20.glUniform1f(loc, values[0])
                        2 -> GLES20.glUniform2f(loc, values[0], values[1])
                        3 -> GLES20.glUniform3f(loc, values[0], values[1], values[2])
                        4 -> GLES20.glUniform4f(loc, values[0], values[1], values[2], values[3])
                        else -> throw IllegalStateException("Wrong number of values! : ${values.size}")
                    }
                }
            }

            //println("draw frame")

            GLES20.glEnableVertexAttribArray(attributeLocation)
            GLES20.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)
            GLES20.glDisableVertexAttribArray(attributeLocation)
            frameCount++
        }
    }

    private fun createAndVerifyShader(shaderCode: String, shaderType: Int): Int {
        val shaderId = GLES20.glCreateShader(shaderType)
        check(shaderId > 0) { "Unable to create shader!" }

        GLES20.glShaderSource(shaderId, shaderCode)
        GLES20.glCompileShader(shaderId)

        val compileStatusArray = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatusArray, 0)
        log("$shaderCode \n : ${GLES20.glGetShaderInfoLog(shaderId)}")

        if (compileStatusArray.first() == 0) {
            GLES20.glDeleteShader(shaderId)
            throw IllegalStateException("Shader compilation failed!")
        }

        return shaderId
    }

    private fun validateProgram(programObjectId: Int): Boolean {
        GLES20.glValidateProgram(programObjectId)
        val validateStatus = IntArray(1)
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0)

        log(
            "Results of validating:" +
                    "${validateStatus[0]} \n  Log : ${
                        GLES20.glGetProgramInfoLog(
                            programObjectId
                        )
                    } \n".trimIndent()
        )

        return validateStatus[0] != 0
    }
}