package de.drick.compose.edgetoedgepreview

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.drick.compose.edgetoedgepreviewlib.CameraCutoutMode
import de.drick.compose.edgetoedgepreviewlib.EdgeToEdgeTemplate
import de.drick.compose.edgetoedgepreviewlib.NavigationMode

@Preview(name = "portrait", device = "spec:width=300dp,height=600dp,dpi=440")
@Preview(name = "landscape", device = "spec:width=300dp,height=600dp,dpi=440,orientation=landscape")
annotation class SampleBlogPreviews

@SampleBlogPreviews
@Composable
fun PreviewEdgeToEdgePortrait() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.ThreeButton,
        cameraCutoutMode = CameraCutoutMode.End,
        showInsetsBorder = true,
    ) {
        PreviewContentAppBar()
    }
}

@SampleBlogPreviews
@Composable
fun PreviewEdgeToEdgePortrait2() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.Gesture,
        cameraCutoutMode = CameraCutoutMode.Middle,
        showInsetsBorder = true,
    ) {
        PreviewContentAppBar()
    }
}

@SampleBlogPreviews
@Composable
fun PreviewEdgeToEdgePortrait3() {
    EdgeToEdgeTemplate(
        navMode = NavigationMode.Gesture,
        cameraCutoutMode = CameraCutoutMode.Middle,
        showInsetsBorder = true,
        isInvertedOrientation = true
    ) {
        PreviewContentAppBar()
    }
}

@Composable
private fun PreviewContentAppBar() {
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        SampleLandscapeContentAppBar()
    } else {
        SamplePortraitContentAppBar()
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
            title = "Main Content",
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal +
                        WindowInsetsSides.Top
            )
        )
        TestComponentWindowInsets(
            modifier = Modifier.fillMaxWidth(),
            innerModifier = Modifier.height(80.dp),
            title = "Navigation Bar",
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
            modifier = Modifier
                .fillMaxHeight()
                .width(140.dp),
            //innerModifier = Modifier.height(80.dp),
            title = "Navigation Bar",
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Start +
                        WindowInsetsSides.Vertical),
            rotatedText = true
        )
        TestComponentWindowInsets(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            title = "Main Content",
            windowInsets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.End +
                        WindowInsetsSides.Vertical
            )
        )
    }
}
