package de.drick.compose.edgetoedgepreviewlib

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lens
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CameraCutoutMode {
    None, Middle, Start, End
}

@Preview(name = "Portrait")
@Preview(name = "Landscape", device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewCameraCutoutVertical() {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val cutoutSize = 80.dp
    if (isLandscape) {
        CameraCutout(
            modifier = Modifier.size(cutoutSize, 400.dp),
            cutoutMode = CameraCutoutMode.Middle,
            isVertical = true
        )
    } else {
        CameraCutout(
            modifier = Modifier.size(400.dp, cutoutSize),
            cutoutMode = CameraCutoutMode.Middle,
            isVertical = false
        )
    }
}

@Composable
fun CameraCutout(
    modifier: Modifier = Modifier,
    cutoutMode: CameraCutoutMode = CameraCutoutMode.Middle,
    isVertical: Boolean = false,
    cutoutSize: Dp = 24.dp
) {
    if (cutoutMode == CameraCutoutMode.None) return
    if (isVertical) {
        val alignment = when (cutoutMode) {
            CameraCutoutMode.None -> Arrangement.Center
            CameraCutoutMode.Middle -> Arrangement.Center
            CameraCutoutMode.Start -> Arrangement.Top
            CameraCutoutMode.End -> Arrangement.Bottom
        }
        Column(
            modifier = modifier
                .padding(4.dp)
                .width(cutoutSize)
                .fillMaxHeight(),
            verticalArrangement = alignment,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                imageVector = Icons.Default.Lens,
                contentDescription = "Camera lens",
            )
        }
    } else {
        val alignment = when (cutoutMode) {
            CameraCutoutMode.None -> Arrangement.Center
            CameraCutoutMode.Middle -> Arrangement.Center
            CameraCutoutMode.Start -> Arrangement.Start
            CameraCutoutMode.End -> Arrangement.End
        }
        Row(
            modifier = modifier
                .padding(4.dp)
                .height(cutoutSize)
                .fillMaxWidth(),
            horizontalArrangement = alignment,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Default.Lens,
                contentDescription = "Camera lens",
            )
        }
    }
}
