package de.drick.compose.sample.ui

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
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
import de.drick.compose.sample.theme.Pink80
import de.drick.compose.sample.theme.Purple80
import de.drick.compose.sample.theme.PurpleGrey80
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.intellij.lang.annotations.Language
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransitionScreen(modifier: Modifier) {
    if (Build.VERSION.SDK_INT > 32) {
        CurtainTransition(
            modifier = modifier.fillMaxSize(),
            content = { TransitionMainScreen() },
            overlay = { TransitionOverlayScreen() }
        )

    } else {
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Unsupported device.", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Device api level (${Build.VERSION.SDK_INT}) to low. API 33 or above required.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Language("AGSL")
val SHADER_SRC = """
uniform float2 iResolution;
uniform float2 iOffset;
uniform shader background;
const float PI = 3.14159265359;

vec4 main(vec2 fragCoord)
{
    vec2 uv = fragCoord / iResolution.x; // change coordinate system 0..1
    vec2 offset = iOffset / iResolution.x;
    //uv.x = pow(uv.x,2.0);
    //offset.x = pow(offset.x, 2.0);
    vec2 a = abs(uv - offset);
    a = a * a * .2;
    float dist = offset.x + a.y * (1.-uv.x);
    float distI = 1.0 - dist;
    float freq = 5.0;
    float intensity = distI * .5;
    float angle = (uv.x / dist) * 2.0 * PI * freq;
    float light = sin(angle) * intensity + 1.0;
    float fold = -cos(angle) * intensity;
    
    vec2 pos = vec2(uv.x / dist, uv.y + fold * .03);
    pos *= iResolution.x;
    vec3 col = background.eval(pos).rgb * light;
    col = mix(col, vec3(0.1), smoothstep(dist, dist + .005, uv.x));
    float shadow = 0.02;
    float alpha = 1.0 - smoothstep(dist, dist + shadow, uv.x);
    return vec4(col * alpha, alpha);
}

""".trimIndent()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CurtainTransition(
    modifier: Modifier,
    content: @Composable () -> Unit,
    overlay: @Composable () -> Unit
) {
    var offset: Offset? by remember { mutableStateOf(null) }
    val shader = remember { RuntimeShader(SHADER_SRC) }
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
                    shader.setFloatUniform("iResolution", size.width, size.height)
                    val iOffset = offset ?: Offset(size.width, 0f)
                    shader.setFloatUniform("iOffset", iOffset.x, iOffset.y)
                    renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "background").asComposeRenderEffect()
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
