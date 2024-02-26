package de.drick.compose.edgetoedgepreviewlib

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Navigation bar")
@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    isVertical: Boolean = false,
    isDarkMode: Boolean = true,
    navMode: NavigationMode = NavigationMode.ThreeButton,
    alpha: Float = 0.5f
) {
    val contentColor = if (isDarkMode) Color.LightGray else Color.DarkGray
    val backgroundColor = if (isDarkMode)
        Color.White.copy(alpha = alpha)
    else
        Color.Black.copy(alpha = alpha)
    val size = 32.dp
    when {
        navMode == NavigationMode.Gesture -> {
            Row(
                modifier = modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                Spacer(
                    Modifier
                        .background(color = contentColor, shape = RoundedCornerShape(4.dp))
                        .width(100.dp)
                        .height(6.dp)
                )
                Spacer(Modifier.weight(1f))
            }
        }
        isVertical -> {
            Column(
                modifier = modifier
                    .background(backgroundColor)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))
                Image(
                    modifier = Modifier.size(size),
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    colorFilter = ColorFilter.tint(contentColor)
                )
                Spacer(Modifier.weight(1f))
                Image(
                    modifier = Modifier.size(size),
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = "Home",
                    colorFilter = ColorFilter.tint(contentColor)
                )
                Spacer(Modifier.weight(1f))
                Image(
                    modifier = Modifier.size(size),
                    imageVector = Icons.Default.Rectangle,
                    contentDescription = "History",
                    colorFilter = ColorFilter.tint(contentColor)
                )
                Spacer(Modifier.weight(1f))
            }
        }
        else -> {
            Row(
                modifier = modifier
                    .background(backgroundColor)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                Image(
                    modifier = Modifier.size(size),
                    imageVector = Icons.Default.Rectangle,
                    contentDescription = "History",
                    colorFilter = ColorFilter.tint(contentColor)
                )
                Spacer(Modifier.weight(1f))
                Image(
                    modifier = Modifier.size(size),
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = "Home",
                    colorFilter = ColorFilter.tint(contentColor)
                )
                Spacer(Modifier.weight(1f))
                Image(
                    modifier = Modifier.size(size),
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    colorFilter = ColorFilter.tint(contentColor)
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
