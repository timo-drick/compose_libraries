package de.drick.compose.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Process
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LifecycleResumeEffect
import de.drick.common.log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10

interface GLDsl {
    fun onSurfaceChanged(block: (width: Int, height: Int) -> Unit)
    fun onDrawFrame(block: () -> Unit)
    fun onDestroy(block: () -> Unit)
}

val simpleOpenGlRenderer = GLRenderer({}) {
    log("Clear view")
    GLES20.glClearColor(1f, 0f, 1f, 1f)

    onDrawFrame {
        log("render frame")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
    onSurfaceChanged { width, height ->
        log("size: $width,$height")
        GLES20.glViewport(0, 0, width, height)
    }
}

interface SurfaceRenderer {
    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun onDrawFrame()
}

class GLRenderer(onErrorFallback: () -> Unit, glBlock: GLDsl.() -> Unit) {

    internal var requestGlRender: () -> Unit = {}
    fun requestRender() {
        requestGlRender()
    }
    fun onResume() {

    }
    fun onPause() {

    }

    class DslImpl: GLDsl {
        var onSurfaceChangedBlock: ((width: Int, height: Int) -> Unit)? = null
        var onDrawFrameBlock: (() -> Unit)? = null
        var onDestroyBlock: (() -> Unit)? = null

        override fun onSurfaceChanged(block: (width: Int, height: Int) -> Unit) {
            check(onSurfaceChangedBlock == null) { "The onSurfaceChanged function can only be used once" }
            onSurfaceChangedBlock = block
        }
        override fun onDrawFrame(block: () -> Unit) {
            check(onDrawFrameBlock == null) { "The onDrawFrame function can only be used once" }
            onDrawFrameBlock = block
        }
        override fun onDestroy(block: () -> Unit) {
            check(onDestroyBlock == null) { "The onDestroy function can only be used once" }
            onDestroyBlock = block
        }
    }

    val renderer = object : SurfaceRenderer {
        private var dsl: DslImpl? = null
        override fun onSurfaceCreated() {
            log("onSurfaceCreated")
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            val egl = EGLContext.getEGL() as EGL10
            //val eglSurfaceInfo = EGLSurfaceInfo(egl, egl.eglGetCurrentContext(), egl.eglGetCurrentDisplay(), config)
            dsl = DslImpl().also { dsl ->
                try {
                    glBlock(dsl)
                } catch (err: Throwable) {
                    log(err)
                    onErrorFallback()
                }
            }
        }

        override fun onSurfaceChanged(width: Int, height: Int) {
            log("onSurfaceChanged")
            dsl?.onSurfaceChangedBlock?.invoke(width, height)

        }

        override fun onDrawFrame() {
            dsl?.onDrawFrameBlock?.invoke()
        }
    }
}

data class EGLSurfaceInfo(val egl: EGL10, val renderContext: EGLContext, val display: EGLDisplay, val config: EGLConfig)


@Composable
fun ComposeGlSurfaceView(
    modifier: Modifier = Modifier,
    renderer: GLRenderer,
    zOrderOnTop: Boolean = true,
    key1: Any? = null
) {
    var view: RenderSurfaceView? = remember {
        null
    }

    /*val k1 = remember(key1) {
        view?.requestRender()
        null
    }*/
    LifecycleResumeEffect(Unit) {
        log("resume")
        view?.let {
            renderer.requestGlRender = {
                it.requestRender()
            }
            it.onResume()
            renderer.onResume()
        }
        onPauseOrDispose {
            log("pause")
            view?.onPause()
            renderer.onPause()
            renderer.requestGlRender = {}
        }
    }
    Box(modifier) {
        AndroidView(
            factory = {
                log("factory")
                val glSurfaceView = RenderSurfaceView(it, null, renderer.renderer)
                glSurfaceView.setZOrderOnTop(zOrderOnTop)
                //glSurfaceView.setZOrderMediaOverlay(true)
                glSurfaceView.debugFlags =
                    GLSurfaceView.DEBUG_CHECK_GL_ERROR or GLSurfaceView.DEBUG_LOG_GL_CALLS
                view = glSurfaceView
                renderer.requestGlRender = {
                    glSurfaceView.requestRender()
                }
                glSurfaceView
            },
            update = { _ ->
            },
            onReset = {
                log("reset")
            }
            ,
            onRelease = {
                log("release")
            }
        )
    }
}

@Composable
fun ComposeGl(
    modifier: Modifier = Modifier,
    renderer: GLRenderer,
    key1: Any? = null
) {
    var view: GLTextureView? = remember {
        null
    }

    LifecycleResumeEffect(Unit) {
        log("resume")
        view?.let {
            it.onResume()
            renderer.onResume()
        }

        onPauseOrDispose {
            log("pause")
            view?.onPause()
            renderer.onPause()
        }
    }
    Box(modifier) {
        key(renderer) {
            AndroidView(
                factory = {
                    log("factory")
                    val glSurfaceView = GLTextureView(it, null, renderer.renderer)
                    glSurfaceView.isOpaque = false
                    view = glSurfaceView
                    glSurfaceView
                },
                update = { _ ->
                    log("update")
                },
                onReset = {
                    log("reset")
                },
                onRelease = {
                    log("release")
                }
            )
        }
    }
}