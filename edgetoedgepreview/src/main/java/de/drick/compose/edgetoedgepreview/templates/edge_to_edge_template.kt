package de.drick.compose.edgetoedgepreview.templates

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
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
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowInsetsCompat

class NavigationPreviewProvider : PreviewParameterProvider<NavigationMode> {
    override val values = sequenceOf(NavigationMode.ThreeButton, NavigationMode.Gesture)
}

enum class NavigationMode {
    Gesture,
    ThreeButton
}

@Composable
fun EdgeToEdgeTemplate(
    navMode: NavigationMode = NavigationMode.ThreeButton,
    cameraCutoutMode: CameraCutoutMode = CameraCutoutMode.Middle,
    isInvertedOrientation: Boolean = false,// in landscape mode it would be that the camera cutout is
                                           // on the right and navigation buttons will be on the left
    showInsetsBorder: Boolean = true,
    content: @Composable () -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isDarkMode = isSystemInDarkTheme()

    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarSize by remember { mutableIntStateOf(0) }
    var cameraCutoutSize by remember { mutableIntStateOf(0) }

    val navigationPos = when {
        navMode == NavigationMode.Gesture -> InsetPos.BOTTOM
        isLandscape && isInvertedOrientation -> InsetPos.LEFT
        isLandscape -> InsetPos.RIGHT
        else -> InsetPos.BOTTOM
    }
    val cameraCutoutPos = when {
        isLandscape.not() && isInvertedOrientation -> InsetPos.BOTTOM
        isLandscape && isInvertedOrientation.not() -> InsetPos.LEFT
        isLandscape -> InsetPos.RIGHT
        else -> InsetPos.TOP
    }

    val windowInsetsState = rememberWindowInsetsState()
    LaunchedEffect(statusBarHeight, navigationBarSize, cameraCutoutSize) {
        windowInsetsState.update {
            setInset(InsetPos.TOP, WindowInsetsCompat.Type.statusBars(), statusBarHeight)
            setInset(
                pos = navigationPos,
                type = WindowInsetsCompat.Type.navigationBars(),
                size = navigationBarSize
            )
            if (cameraCutoutMode != CameraCutoutMode.None) {
                setInset(
                    pos = cameraCutoutPos,
                    type = WindowInsetsCompat.Type.displayCutout(),
                    size = cameraCutoutSize
                )
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        val borderModifier = if (showInsetsBorder) Modifier.border(2.dp, Color.Red) else Modifier
        val cameraCutoutAlignment = when(cameraCutoutPos) {
            InsetPos.LEFT -> AbsoluteAlignment.CenterLeft
            InsetPos.TOP -> AbsoluteAlignment.TopLeft
            InsetPos.RIGHT -> AbsoluteAlignment.CenterRight
            InsetPos.BOTTOM -> AbsoluteAlignment.BottomLeft
        }
        CameraCutout(
            modifier = Modifier
                .zIndex(1001f)
                .align(cameraCutoutAlignment)
                .then(borderModifier)
                .onSizeChanged {
                    cameraCutoutSize = if (isLandscape) it.width else it.height
                },
            cutoutMode = cameraCutoutMode,
            isVertical = isLandscape
        )
        StatusBar(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .zIndex(1000f)
                .align(Alignment.TopCenter)
                .then(borderModifier)
                .onSizeChanged {
                    statusBarHeight = it.height
                },
            isDarkMode = isDarkMode
        )
        val navigationBarAlignment = when(navigationPos) {
            InsetPos.LEFT -> AbsoluteAlignment.CenterLeft
            InsetPos.TOP -> AbsoluteAlignment.TopLeft
            InsetPos.RIGHT -> AbsoluteAlignment.CenterRight
            InsetPos.BOTTOM -> AbsoluteAlignment.BottomLeft
        }
        NavigationBar(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.displayCutout.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                ))
                .zIndex(1000f)
                .align(navigationBarAlignment)
                .then(borderModifier)
                .onSizeChanged {
                    val cutoutPadding = if (cameraCutoutPos == InsetPos.BOTTOM) cameraCutoutSize else 0
                    navigationBarSize = if (navigationPos == InsetPos.BOTTOM) {
                        it.height + cutoutPadding
                    } else {
                        it.width
                    }
                },
            isVertical = isLandscape,
            isDarkMode = isDarkMode,
            navMode = navMode
        )
        content()
    }
}
