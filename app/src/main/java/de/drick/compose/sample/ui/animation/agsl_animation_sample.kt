package de.drick.compose.sample.ui.animation

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ShaderBrush
import de.drick.compose.opengl.ComposeGl
import de.drick.compose.opengl.PixelShader

@Composable
fun ShaderAnimation(
    modifier: Modifier,
    shaderSrc: String = SHADER_SPINNER_SPHERE_3D,
    durationMillis: Int = 2000
) {
    // declare the ValueAnimator
    val infiniteTransition = rememberInfiniteTransition("animation")
    val animation = infiniteTransition.animateFloat(
        label = "loop_0_1",
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis, easing = LinearEasing),
        )
    )
    if (Build.VERSION.SDK_INT > 32) {
        val shader = remember { RuntimeShader(shaderSrc) }
        val brush = remember(shader) { ShaderBrush(shader) }
        Canvas(modifier = modifier) {
            shader.setFloatUniform("iTime", animation.value)
            shader.setFloatUniform("iResolution", size.width, size.height)
            drawRect(brush)
        }
    } else {
        val pixelShader = remember {
            PixelShader(shaderSrc)
        }
        pixelShader.setFloatUniform("iTime", animation.value)
        ComposeGl(
            modifier = modifier,
            renderer = pixelShader.renderer
        )
    }
}

@RequiresApi(33)
@Composable
fun Modifier.loadingModifier(src: String = SHADER_SPINNER_SPHERE_3D) = composed {
    val infiniteTransition = rememberInfiniteTransition("loop")
    val animation = infiniteTransition.animateFloat(
        label = "progress",
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = LinearEasing),
        )
    )
    val shader = remember { RuntimeShader(src) }
    val brush = remember(shader) { ShaderBrush(shader) }
    this.drawWithCache {
        shader.setFloatUniform("iTime", animation.value)
        shader.setFloatUniform("iResolution", size.width, size.height)
        onDrawWithContent {
            drawRect(brush, blendMode = BlendMode.Screen)
        }
    }
}
