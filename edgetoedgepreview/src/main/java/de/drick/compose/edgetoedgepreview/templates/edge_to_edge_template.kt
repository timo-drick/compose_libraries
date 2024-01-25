package de.drick.compose.edgetoedgepreview.templates

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.zIndex
import androidx.core.view.WindowInsetsCompat

class NavigationPreviewProvider : PreviewParameterProvider<NavigationMode> {
    override val values = sequenceOf(NavigationMode.ThreeButton, NavigationMode.Gesture)
}

enum class NavigationMode {
    Gesture, ThreeButton
}

@Composable
fun EdgeToEdgeTemplate(
    navMode: NavigationMode = NavigationMode.ThreeButton,
    content: @Composable () -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isDarkMode = isSystemInDarkTheme()

    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarSize by remember { mutableIntStateOf(0) }

    val isBottomNavigationBar = navMode == NavigationMode.Gesture || isLandscape.not()

    val windowInsetsState = rememberWindowInsetsState()
    LaunchedEffect(statusBarHeight, navigationBarSize) {
        windowInsetsState.update {
            setInset(InsetPos.TOP, WindowInsetsCompat.Type.statusBars(), statusBarHeight)
            setInset(
                pos = if (isBottomNavigationBar) InsetPos.BOTTOM else InsetPos.RIGHT,
                type = WindowInsetsCompat.Type.navigationBars(),
                size = navigationBarSize
            )
        }
    }
    Box(Modifier.fillMaxSize()) {
        StatusBar(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Right))
                .zIndex(1000f)
                .align(Alignment.TopCenter)
                .onSizeChanged {
                    statusBarHeight = it.height
                },
            isDarkMode = isDarkMode
        )
        NavigationBar(
            modifier = Modifier
                .zIndex(1000f)
                .align(if (isBottomNavigationBar) Alignment.BottomCenter else Alignment.CenterEnd)
                .onSizeChanged {
                    navigationBarSize = if (isBottomNavigationBar) it.height else it.width
                },
            isVertical = isLandscape,
            isDarkMode = isDarkMode,
            navMode = navMode
        )
        content()
    }
}

@Composable
fun UseCase() {
    WindowInsetsEmulator(
        {

        },
        {

        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Green))
    }
}

@Composable
fun WindowInsetsEmulator(
    vararg insets: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {

}