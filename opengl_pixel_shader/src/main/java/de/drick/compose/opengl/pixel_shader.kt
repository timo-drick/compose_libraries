package de.drick.compose.opengl

import android.opengl.GLES10
import android.opengl.GLES31
import androidx.compose.ui.graphics.Color
import de.drick.common.log
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
        vec2 uv = fragCoord*iResolution.x/iResolution.y;
    
        // Time varying pixel color
        vec3 col = 0.5 + 0.5*cos(uv.xyx+vec3(0,2,4));
    
        // Output to screen
        gl_FragColor = vec4(col,1.0);
    }
""".trimIndent()


/**
 * Shader that uses a rectangular geometry which
 */
class PixelShader(
    agslShaderSrc: String = simpleFragmentShader,
    onErrorFallback: () -> Unit = {}
) {
    private val agslPrefix = "AGSLXXYY"

    @Language("GLSL")
    val vertexSource = """
    attribute vec4 vPosition;
    varying vec2 fragCoord$agslPrefix;
    uniform vec2 iResolution;
    void main() {
       gl_Position = vPosition;
       vec2 texPos = vec2(vPosition.x, -vPosition.y) / 2.0 + .5;
       fragCoord$agslPrefix = texPos * iResolution;
    }
    """

    private val fragmentShaderSrc = createGLSLCode(agslShaderSrc)

    private fun createGLSLCode(agslSrc: String): String {
        val converted = agslSrc
            .replace("half2", "vec2")
            .replace("half3", "vec3")
            .replace("half4", "vec4")
            .replace("float2", "vec2")
            .replace("float3", "vec3")
            .replace("float4", "vec4")
            .replace("main(", "main$agslPrefix(")
        @Language("GLSL")
        val tmp = """
        precision highp float;
        
        $converted
        
        varying vec2  fragCoord$agslPrefix;
        
        void main() {
            vec2 agslCoord = fragCoord$agslPrefix;
            gl_FragColor = main$agslPrefix(agslCoord);
        }
    """.trimIndent()
        return tmp
    }

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
    fun setFloatUniform(name: String, value1: Float) {
        setFloatUniform(name, arrayOf(value1))
    }
    fun setFloatUniform(name: String, value1: Float, value2: Float) {
        setFloatUniform(name, arrayOf(value1, value2))
    }
    fun setFloatUniform(name: String, value1: Float, value2: Float, value3: Float) {
        setFloatUniform(name, arrayOf(value1, value2, value3))
    }
    fun setColorUniform(name: String, color: Color) {
        setFloatUniform(name, arrayOf(color.red, color.green, color.blue, color.alpha))
    }
    private fun setFloatUniform(name: String, values: Array<Float>) {
        registerUniform(name)
        uniformFloatValues[name] = values
        renderer.requestRender()
    }

    val renderer = GLRenderer(onErrorFallback = onErrorFallback) {
        GLES31.glClearColor(0f, 0f, 0f, 1f)
        GLES31.glDisable(GL10.GL_DITHER)
        GLES31.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)

        val programId = GLES31.glCreateProgram()
        require(programId > 0) { "Unable to create program!" }
        val fragShader = createAndVerifyShader(fragmentShaderSrc, GLES31.GL_FRAGMENT_SHADER)
        val vertShader = createAndVerifyShader(vertexSource, GLES31.GL_VERTEX_SHADER)
        GLES31.glAttachShader(programId, fragShader)
        GLES31.glAttachShader(programId, vertShader)
        GLES31.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES31.glGetProgramiv(programId, GLES31.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val msg = "Linking of program failed. ${GLES31.glGetProgramInfoLog(programId)}"
            GLES31.glDeleteProgram(programId)
            throw IllegalStateException(msg)
        }

        if (validateProgram(programId).not()) throw IllegalStateException("Program validation failed!")

        val attributeLocation = GLES31.glGetAttribLocation(programId, "vPosition")
        uniforms.forEach { name -> // get location for uniforms
            val loc = GLES31.glGetUniformLocation(programId, name)
            uniformLocations[name] = loc
        }

        val resolutionUniform = GLES31.glGetUniformLocation(programId, "iResolution")

        verticesData.position(0)
        GLES31.glVertexAttribPointer(
            attributeLocation,
            positionComponentCount,
            GLES31.GL_FLOAT,
            false,
            0,
            verticesData
        )

        GLES31.glDetachShader(programId, vertShader)
        GLES31.glDetachShader(programId, fragShader)
        GLES31.glDeleteShader(vertShader)
        GLES31.glDeleteShader(fragShader)

        var surfaceWidth = 0f
        var surfaceHeight = 0f

        onSurfaceChanged { width, height ->
            GLES31.glViewport(0, 0, width, height)
            surfaceWidth = width.toFloat()
            surfaceHeight = height.toFloat()
        }

        var frameCount = 0
        onDrawFrame {
            GLES31.glDisable(GL10.GL_DITHER)
            GLES31.glClear(GL10.GL_COLOR_BUFFER_BIT)

            GLES31.glUseProgram(programId)
            GLES31.glUniform2f(resolutionUniform, surfaceWidth, surfaceHeight)

            //set int uniforms
            uniformIntValues.forEach { (name, values) ->
                uniformLocations[name]?.let { loc ->
                    //println("setUniform($name, ${values.joinToString()}) pos: $loc")
                    when (values.size) {
                        1 -> GLES31.glUniform1i(loc, values[0])
                        2 -> GLES31.glUniform2i(loc, values[0], values[1])
                        3 -> GLES31.glUniform3i(loc, values[0], values[1], values[2])
                        4 -> GLES31.glUniform4i(loc, values[0], values[1], values[2], values[3])
                        else -> throw IllegalStateException("Wrong number of values! : ${values.size}")
                    }
                }
            }
            uniformFloatValues.forEach { (name, values) ->
                uniformLocations[name]?.let { loc ->
                    //println("setUniform($name, ${values.joinToString()}) pos: $loc")
                    when (values.size) {
                        1 -> GLES31.glUniform1f(loc, values[0])
                        2 -> GLES31.glUniform2f(loc, values[0], values[1])
                        3 -> GLES31.glUniform3f(loc, values[0], values[1], values[2])
                        4 -> GLES31.glUniform4f(loc, values[0], values[1], values[2], values[3])
                        else -> throw IllegalStateException("Wrong number of values! : ${values.size}")
                    }
                }
            }

            //println("draw frame")

            GLES31.glEnableVertexAttribArray(attributeLocation)
            GLES31.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)
            GLES31.glDisableVertexAttribArray(attributeLocation)
            frameCount++
        }
    }

    private fun createAndVerifyShader(shaderCode: String, shaderType: Int): Int {
        val version = GLES10.glGetString(GL10.GL_VERSION)
        log("opengl version: $version")

        val shaderId = GLES31.glCreateShader(shaderType)
        check(shaderId > 0) { "Unable to create shader!" }

        GLES31.glShaderSource(shaderId, shaderCode)
        GLES31.glCompileShader(shaderId)

        val compileStatusArray = IntArray(1)
        GLES31.glGetShaderiv(shaderId, GLES31.GL_COMPILE_STATUS, compileStatusArray, 0)

        if (compileStatusArray.first() == 0) {
            log("$shaderCode \n : ${GLES31.glGetShaderInfoLog(shaderId)}")
            GLES31.glDeleteShader(shaderId)
            throw IllegalStateException("Shader compilation failed!")
        }

        return shaderId
    }

    private fun validateProgram(programObjectId: Int): Boolean {
        GLES31.glValidateProgram(programObjectId)
        val validateStatus = IntArray(1)
        GLES31.glGetProgramiv(programObjectId, GLES31.GL_VALIDATE_STATUS, validateStatus, 0)
        val valid = validateStatus[0] != 0
        if (valid.not()) {
            log(
                "Results of validating:" +
                        "${validateStatus[0]} \n  Log : ${
                            GLES31.glGetProgramInfoLog(
                                programObjectId
                            )
                        } \n".trimIndent()
            )
        }
        return valid
    }
}