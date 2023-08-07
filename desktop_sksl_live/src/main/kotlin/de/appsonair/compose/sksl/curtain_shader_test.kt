package de.appsonair.compose.sksl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeShaderBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun main() = singleWindowApplication {
    MaterialTheme(colorScheme = DarkColorScheme) {
        Scaffold { padding ->
            CurtainTransition(
                modifier = Modifier.padding(padding).fillMaxSize(),
                content = { TransitionMainScreen() },
                overlay = { TransitionOverlayScreen() }
            )
        }
    }
}

val file = File("desktop_sksl_live/curtain_shader.glsl")

@Composable
fun CurtainTransition(
    modifier: Modifier,
    content: @Composable () -> Unit,
    overlay: @Composable () -> Unit
) {
    var offset: Offset? by remember { mutableStateOf(null) }
    val effect = rememberLiveEffect(file)
    val shaderBuilder = remember(effect) { RuntimeShaderBuilder(effect) }
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    val pos = change.position
                    change.consume()
                    offset = pos
                }
            }
    ) {
        content()
        Box(
            modifier = Modifier
                .graphicsLayer {
                    shaderBuilder.uniform("iResolution", size.width, size.height)
                    val iOffset = offset ?: Offset(size.width, 0f)
                    shaderBuilder.uniform("iOffset", iOffset.x, iOffset.y)
                    renderEffect = ImageFilter.makeRuntimeShader(
                        runtimeShaderBuilder = shaderBuilder,
                        shaderName = "background",
                        input = null
                    ).asComposeRenderEffect()
                }
        ) {
            overlay()
        }
    }
}

@Composable
fun TransitionMainScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Card {
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = "Formatting",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    var checked by remember { mutableStateOf(false) }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "Metric Units",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = checked, onCheckedChange = { checked = checked.not() })
                }
            }
        }
    }
}

@Composable
fun TransitionItem(content: @Composable () -> Unit) {
    Spacer(Modifier.height(8.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
        content = content
    )
}

@Composable
fun TransitionOverlayScreen() {
    MaterialTheme(colorScheme = DarkBlueColorScheme) {
        var currentTime: String by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            val sdf = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.US)
            while (isActive) {
                currentTime = sdf.format(Date())
                delay(1000)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ) {
            Text("Frankfurt, Germany", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            TransitionItem {
                Column(Modifier.padding(8.dp)) {
                    Text(
                        text = "Current time in Frankfurt, Germany",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            TransitionItem {
                Column(Modifier.padding(8.dp)) {
                    Text(
                        text = "Forecast",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Spacer(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                            .height(1.dp)
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Today Rain",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tomorrow Rain",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Thu Rain",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val DarkBlueColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF00004B),
    onBackground = Color(0xFFFAFFFE),
    surface = Color(0xFF18165F),
    onSurface = Color(0xFFFAFFFE),
    onSurfaceVariant = Color(0xFF54519B)
)
