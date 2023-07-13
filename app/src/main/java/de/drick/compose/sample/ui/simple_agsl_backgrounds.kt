package de.drick.compose.sample.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.drick.common.log
import de.drick.compose.opengl.ComposeGl
import de.drick.compose.opengl.PixelShader
import org.intellij.lang.annotations.Language

@Language("AGSL")
val shaderCheckerBox1 = """
    vec3 color1 = vec3(0.1);
    vec3 color2 = vec3(0.9);
    
    vec3 checkBoard(vec2 uv) {
        vec2 id = floor(uv);
        float w = fract((id.x + id.y)/2.) * 2.;
        return mix(color1.rgb, color2.rgb, w);
    }
    half4 main(float2 fragCoord) {
      return half4(checkBoard(fragCoord/100.), 1);
    }
""".trimIndent()

@Composable
fun SimpleShader() {

}

@RequiresApi(33)
fun Modifier.backgroundShader(shaderSrc: String): Modifier = this.drawWithCache {
    val shader = RuntimeShader(shaderSrc)
    val brush = ShaderBrush(shader)
    onDrawBehind {
        drawRect(brush, blendMode = BlendMode.Screen)
    }
}

@Language("AGSL")
val shaderCheckerBox2 = """
    uniform float2 iScaling;
    
    vec3 color1 = vec3(0.1);
    vec3 color2 = vec3(0.9);
    
    vec3 checkBoard(vec2 uv) {
        vec2 id = floor(uv);
        float w = fract((id.x + id.y)/2.) * 2.;
        return mix(color1.rgb, color2.rgb, w);
    }
    half4 main(float2 fragCoord) {
      return half4(checkBoard(fragCoord * iScaling), 1);
    }
""".trimIndent()

@RequiresApi(33)
fun Modifier.backgroundShaderScaling(shaderSrc: String): Modifier = this.drawWithCache {
    val shader = RuntimeShader(shaderSrc)
    val x = 10f/size.width
    val y = 10f/size.height
    shader.setFloatUniform("iScaling", x, y)
    val brush = ShaderBrush(shader)
    onDrawBehind {
        drawRect(brush)
    }
}

@RequiresApi(33)
fun Modifier.bgDensityShader(shaderSrc: String, width: Dp) = composed {
    val dpPixel = with(LocalDensity.current) { width.toPx() }
    this.drawWithCache {
        val shader = RuntimeShader(shaderSrc)
        val x = 10f / dpPixel //10 checkerboard pattern per width dp
        shader.setFloatUniform("iScaling", x, x)
        val brush = ShaderBrush(shader)
        onDrawBehind {
            drawRect(brush)
        }
    }
}


@Composable
fun CompatShader(modifier: Modifier = Modifier, shaderSrc: String) {
    val density = LocalDensity.current.density
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    log("Pixel density: $density")
    log("Display $screenWidthDp dp x $screenHeightDp dp")
    val dpWidth = 393.dp
    val dpPixel = with(LocalDensity.current) { dpWidth.toPx() }
    val x = 10f / dpPixel
    val y = x
    if (Build.VERSION.SDK_INT >= 33 && false) {
        Spacer(
            modifier = modifier.drawWithCache {
                val shader = RuntimeShader(shaderSrc)
                shader.setFloatUniform("iScaling", x, y)
                val brush = ShaderBrush(shader)
                onDrawBehind {
                    drawRect(brush)
                }
            }
        )
    } else {
        val pixelShader = remember {
            PixelShader(shaderSrc)
        }
        pixelShader.setFloatUniform("iScaling", x, y)
        ComposeGl(renderer = pixelShader.renderer, modifier = modifier)
    }
}
