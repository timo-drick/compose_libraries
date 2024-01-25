package de.drick.compose.edgetoedgepreview.templates

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
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Navigation bar")
@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    isVertical: Boolean = false,
    isDarkMode: Boolean = false,
    navMode: NavigationMode = NavigationMode.ThreeButton,
    alpha: Float = 0.5f
) {
    val contentColor = if (isDarkMode) Color.Gray else Color.DarkGray
    val backgroundColor = if (isDarkMode)
        Color.Black.copy(alpha = alpha)
    else
        Color.White.copy(alpha = alpha)
    val size = 32.dp
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
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
                    Icon(
                        modifier = Modifier.size(size),
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        modifier = Modifier.size(size),
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = "Home"
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        modifier = Modifier.size(size),
                        imageVector = Icons.Default.Rectangle,
                        contentDescription = "History"
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
                    Icon(
                        modifier = Modifier.size(size),
                        imageVector = Icons.Default.Rectangle,
                        contentDescription = "History"
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        modifier = Modifier.size(size),
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = "Home"
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        modifier = Modifier.size(size),
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
