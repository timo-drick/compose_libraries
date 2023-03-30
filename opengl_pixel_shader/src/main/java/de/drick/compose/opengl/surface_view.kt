package de.drick.compose.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import de.drick.common.log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

@SuppressLint("ViewConstructor")
class RenderSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    renderer: Renderer,
) : GLSurfaceView(context, attrs) {

    init {

        /* Setup the context factory for 2.0 rendering.
         * See ContextFactory class definition below */
        setEGLContextFactory(ContextFactory())

        setRenderer(renderer)

        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }

    override fun onResume() {
        super.onResume()
        log("onResume")
    }

    override fun onPause() {
        super.onPause()
        log("onPause")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        log("ShaderGLSurfaceView onDetachedFromWindow")
    }
}

private class ContextFactory : GLSurfaceView.EGLContextFactory {
    private val EGL_CONTEXT_CLIENT_VERSION = 0x3098

    override fun createContext(egl: EGL10, display: EGLDisplay, eglConfig: EGLConfig): EGLContext {
        log("creating OpenGL ES 2.0 context")
        checkEglError("Before eglCreateContext", egl)
        val attrib_list = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        val context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list)
        checkEglError("After eglCreateContext", egl)
        //eglSurfaceInfo = EGLSurfaceInfo(egl, this@GL2View, context, display, eglConfig)
        return context
    }

    override fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext) {
        log("destroy OpenGL ES 2.0 context")
        egl.eglDestroyContext(display, context)
        //eglSurfaceInfo = null
    }
    private fun checkEglError(prompt: String, egl: EGL10) {
        var error = egl.eglGetError()
        while (error != EGL10.EGL_SUCCESS) {
            log(String.format("%s: EGL error: 0x%x", prompt, error))
            error = egl.eglGetError()
        }
    }
}