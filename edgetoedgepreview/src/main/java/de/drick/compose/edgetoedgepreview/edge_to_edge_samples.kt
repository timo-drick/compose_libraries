package de.drick.compose.edgetoedgepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.drick.compose.edgetoedgepreviewlib.CameraCutoutMode
import de.drick.compose.edgetoedgepreviewlib.EdgeToEdgeTemplate
import de.drick.compose.edgetoedgepreviewlib.NavigationMode

@Preview(device = "spec:parent=pixel_5,orientation=portrait")
@Composable
fun PreviewEdgeToEdgePortrait() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.ThreeButton,
        cameraCutoutMode = CameraCutoutMode.Middle,
        showInsetsBorder = true,
    ) {
        SamplePortraitContentAppBar()
    }
}
@Preview(device = "spec:parent=pixel_5,orientation=portrait")
@Composable
fun PreviewEdgeToEdgePortrait2() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.Gesture,
        cameraCutoutMode = CameraCutoutMode.Middle,
        showInsetsBorder = true,
    ) {
        SamplePortraitContentAppBar()
    }
}
@Preview(device = "spec:parent=pixel_5,orientation=portrait")
@Composable
fun PreviewEdgeToEdgePortrait3() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.Gesture,
        cameraCutoutMode = CameraCutoutMode.Middle,
        showInsetsBorder = true,
        isInvertedOrientation = true
    ) {
        SamplePortraitContentAppBar()
    }
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewEdgeToEdgeLandscape() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.ThreeButton,
        cameraCutoutMode = CameraCutoutMode.Middle,
        showInsetsBorder = true,
        isStatusBarVisible = true,
        isNavigationBarVisible = true,
        isInvertedOrientation = false
    ) {
        SampleLandscapeContentAppBar()
    }
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewEdgeToEdgeLandscape3() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.ThreeButton,
        cameraCutoutMode = CameraCutoutMode.Middle,
        showInsetsBorder = true,
        isStatusBarVisible = true,
        isNavigationBarVisible = true,
        isInvertedOrientation = true
    ) {
        SampleLandscapeContentAppBar()
    }
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewEdgeToEdgeLandscape2() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.Gesture,
        cameraCutoutMode = CameraCutoutMode.Start,
        showInsetsBorder = true,
    ) {
        SampleLandscapeContentAppBar()
    }
}
@Composable
fun SamplePortraitContentAppBar() {
    Column(
        Modifier
            .background(Color.LightGray)
            .fillMaxSize()
    ) {
        TestComponentWindowInsets(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            title = "Main content",
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal +
                        WindowInsetsSides.Top
            )
        )
        TestComponentWindowInsets(
            modifier = Modifier.fillMaxWidth(),
            innerModifier = Modifier.height(80.dp),
            title = "App navigation bar",
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal +
                        WindowInsetsSides.Bottom
            )
        )
    }
}

@Composable
fun SampleLandscapeContentAppBar() {
    Row(
        Modifier
            .background(Color.LightGray)
            .fillMaxSize()
    ) {
        TestComponentWindowInsets(
            modifier = Modifier.fillMaxHeight().width(140.dp),
            //innerModifier = Modifier.height(80.dp),
            title = "App navigation bar",
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Start +
                        WindowInsetsSides.Vertical),
            rotatedText = true
        )
        TestComponentWindowInsets(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            title = "Main content",
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.End +
                        WindowInsetsSides.Vertical
            )
        )
    }
}
