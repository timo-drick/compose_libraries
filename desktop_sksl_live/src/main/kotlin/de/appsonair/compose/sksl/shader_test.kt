package de.appsonair.compose.sksl

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.intellij.lang.annotations.Language
import org.jetbrains.skia.RuntimeShaderBuilder
import java.io.File
import java.util.*

@Language("AGSL")
val appsOnAirBackground = """
    layout(color) uniform vec3 background;
    layout(color) uniform vec3 primary;
    //uniform vec3 background;
    //uniform vec3 primary;
    
    float PI = 3.14159265359;

    //vec3 background = vec3(254./255., 238./255., 210./255.);
    //vec3 background = vec3(250./255., 195./255., 103./255.);
    //vec3 primary = vec3(244./255., 159./255., 13./255.);

    float dSine(vec2 uv, vec2 offset, float amplitude) {
        float a = 2.5;
        return uv.y + offset.y - cos(sin(uv.x*a*1.1)*a + PI + offset.x) * amplitude*1.6;
    }

    float dMainLine(vec2 uv, vec2 offset, float amplitude) {
        float diff = dSine(uv, offset, amplitude);
        float d = min(smoothstep(0.15, 0.0, diff), smoothstep(-0.005, 0.0, diff));
        return clamp(d, 0.0, 1.0);
    }

    float dMaskLine(vec2 uv, vec2 offset, float amplitude) {
        float diff = dSine(uv, offset, amplitude);
        float d = min(smoothstep(.2, 0.0, diff), smoothstep(-.2, 0.0, diff));
        return clamp(d, 0.0, 1.0);
    }

    half4 main(vec2 fragcoord) {
        float y = fract(fragcoord.y/400.)*400. + 250.;
        float density = 800.;
        vec2 uv = .5 - vec2(fragcoord.x, y) / density;
        uv *= vec2(1.0, 4.0);
        float d = 0.0;
        float mask = 0.0;
        
        uv.x += 0.9;
        
        d = max(d, dMainLine(uv, vec2(.0, .0), 0.3)*0.8);
        d = max(d, dMainLine(uv, vec2(-.03, .033), 0.33)*0.6);
        mask = max(mask, dMaskLine(uv, vec2(.0, .0), 0.3)*0.8);
        float bshift = .2;
        d = max(d, dMainLine(uv, vec2(-.2, .10), 0.3)*0.3);
        mask = max(mask, dMaskLine(uv, vec2(-.2, .10), 0.3)*0.3);
        d = max(d, dMainLine(uv, vec2(-.25, .15), 0.34)*0.2);
        
        //d = clamp(d, .0, 1.);
        
        vec3 colLine = mix(vec3(1.), primary, d);
        vec3 col = mix(background, colLine, mask);
        
        return half4(col, 1.0);
    }
""".trimIndent()

fun Modifier.aoaBackground(): Modifier = composed {
    val background = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val file = File("/home/timo/projects/compose/github/rewe_ebon_analyzer/test.glsl")
    val runtimeEffect = rememberLiveEffect(file)
    this.drawWithCache {
        //val runtimeEffect = RuntimeEffect.makeForShader(appsOnAirBackground)

        val builder = RuntimeShaderBuilder(runtimeEffect)
        builder.uniformColor("background", background)
        builder.uniformColor("primary", primary)
        val shader = builder.makeShader()
        val brush = ShaderBrush(shader)
        onDrawBehind {
            drawRect(brush = brush, topLeft = Offset.Zero, size = size)
        }
    }
}


fun main() = application {
    val state = rememberWindowState(
        placement = WindowPlacement.Floating,
        position = WindowPosition.Aligned(Alignment.Center),
        size = DpSize(1000.dp, 600.dp)
    )


    Window(
        title = "Compose / Skia shader demo",
        state = state,
        onCloseRequest = ::exitApplication,
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val time = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            )
        )
        EbonTheme {
            val file = File("desktop_sksl_live/test.glsl")
            val background = MaterialTheme.colorScheme.background
            val primary = MaterialTheme.colorScheme.primary
            val effect = rememberLiveEffect(file)
            Box(
                modifier = Modifier.fillMaxSize()
                    //.background(background)
                    .skslBackground(
                        effect = effect,
                        iTime = time.value,
                        uniforms = { builder ->
                            builder.uniformColor("background", background)
                            builder.uniformColor("primary", primary)
                            builder.uniform("iDensity", 1f)
                        }
                    )
                //.skslLiveBackground(file)
            ) {
                val spinnerEffect = rememberLiveEffect(File("desktop_sksl_live/arrow.glsl"))
                Box(
                    modifier = Modifier.align(Alignment.Center).fillMaxSize().skslBackground(spinnerEffect)
                )

            }
        }
    }
}

fun RuntimeShaderBuilder.uniformColor(name: String, color: Color) {
    val c = color.convert(ColorSpaces.ExtendedSrgb)
    uniform(name, c.red, c.green, c.blue, 1f)
}
