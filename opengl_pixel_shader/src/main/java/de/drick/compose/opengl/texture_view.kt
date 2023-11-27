package de.drick.compose.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14.*
import android.util.AttributeSet
import android.view.TextureView
import de.drick.common.log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface


@SuppressLint("ViewConstructor")
class GLTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val renderer: SurfaceRenderer,
): TextureView(context, attrs) {

    private val config = intArrayOf(
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8,
        EGL_DEPTH_SIZE, 16,
        EGL_STENCIL_SIZE, 0,
        EGL_NONE
    )

    fun requestRender() {
        //TODO
    }

    fun onResume() {
        //TODO
    }

    fun onPause() {
        //TODO
    }


    data class SurfaceContext(
        val egl: EGL10,
        val eglDisplay: EGLDisplay,
        val eglConfig: EGLConfig,
        val eglContext: EGLContext,
        val eglSurface: EGLSurface
    )

    init {
        surfaceTextureListener = object : SurfaceTextureListener {

            var surfaceContext: SurfaceContext? = null

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                log("create surface")
                val egl = EGLContext.getEGL() as EGL10
                val eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
                if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
                    throw RuntimeException("eglGetDisplay failed")
                }
                if (egl.eglInitialize(eglDisplay, intArrayOf(0, 0)).not()) {
                    throw RuntimeException("eglInitialize failed")
                }
                val eglConfig = chooseEglConfig(egl, eglDisplay);

                val attrib_list = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 3, EGL10.EGL_NONE)
                val eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list)
                val eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surface, null)

                surfaceContext = SurfaceContext(egl, eglDisplay, eglConfig, eglContext, eglSurface)
                log("surface created")

                egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
                renderer.onSurfaceCreated()
                renderer.onSurfaceChanged(width, height)
                egl.eglSwapBuffers(eglDisplay, eglSurface)

            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                log("size changed: $width x $height")
                surfaceContext?.let { context ->
                    context.egl.eglMakeCurrent(context.eglDisplay, context.eglSurface, context.eglSurface, context.eglContext)
                    renderer.onSurfaceChanged(width, height)
                    context.egl.eglSwapBuffers(context.eglDisplay, context.eglSurface)
                }
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                log("surface destroyed")
                surface.release()
                surfaceContext?.let { context ->
                    context.egl.eglDestroyContext(context.eglDisplay, context.eglContext)
                    context.egl.eglDestroySurface(context.eglDisplay, context.eglSurface)
                }
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                surfaceContext?.let { context ->
                    context.egl.eglMakeCurrent(context.eglDisplay, context.eglSurface, context.eglSurface, context.eglContext)
                    renderer.onDrawFrame()
                    context.egl.eglSwapBuffers(context.eglDisplay, context.eglSurface)
                }
            }
        }
    }

    internal fun chooseEglConfig(egl: EGL10, eglDisplay: EGLDisplay) : EGLConfig {
        val configsCount = intArrayOf(0);
        val configs = arrayOfNulls<EGLConfig>(1);
        egl.eglChooseConfig(eglDisplay, config, configs, 1, configsCount)
        return configs[0]!!
    }
}
