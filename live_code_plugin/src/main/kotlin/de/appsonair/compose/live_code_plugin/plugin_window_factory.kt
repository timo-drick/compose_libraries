package de.appsonair.compose.live_code_plugin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.components.BorderLayoutPanel
import de.appsonair.compose.live_code_plugin.theme.WidgetTheme

class WindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(
            LiveCodeToolWindow(project, toolWindow), "", false
        )
        toolWindow.contentManager.addContent(content)
    }
}

class LiveCodeToolWindow(
    private val project: Project,
    private val toolWindow: ToolWindow
) : BorderLayoutPanel(), Disposable {
    private val remoteLiveService by lazy { service<RemoteLiveService>() }

    init {
        println("Create toolwindow")
        val composePanel = ComposePanel().apply {
            setBounds(0, 0, 800, 600)
            setContent {
                WidgetTheme(darkTheme = true) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainScreen(project.basePath ?: "", remoteLiveService)
                    }
                }
            }
        }
        addToCenter(composePanel)
    }

    override fun dispose() {
        println("Disposed")
    }
}