package de.drick.compose.sample.ui.animation

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import de.drick.common.log
import de.drick.compose.opengl.ComposeGl
import de.drick.compose.opengl.PixelShader
import de.drick.compose.sample.ui.remoteAssetAsState
import org.intellij.lang.annotations.Language

@Composable
fun ShinyButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues()
    ) {
        val buttonShineShaderSrc = remoteAssetAsState("shader/button_shine.agsl")
        if (Build.VERSION.SDK_INT >= 33) {
            Box(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
                    .shiny(buttonShineShaderSrc)
                    .padding(contentPadding)
            ) {
                content()
            }
        } else {
            Box(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
            ) {
                ButtonShader(
                    modifier = Modifier.fillMaxSize(),
                    shaderSrc = buttonShineShaderSrc
                )
                Box(Modifier.padding(contentPadding)) {
                    content()
                }
            }
        }
    }

}

@RequiresApi(33)
fun Modifier.shiny(shaderSrc: String) = composed {
    val infiniteTransition = rememberInfiniteTransition("animation")
    val animation = infiniteTransition.animateFloat(
        label = "loop_0_1",
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1300, easing = LinearEasing),
        )
    )
    drawWithCache {
        val brush = try {
            val shader = RuntimeShader(shaderSrc)
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", animation.value)
            ShaderBrush(shader)
        } catch (err: Throwable) {
            log(err)
            null
        }
        onDrawBehind {
            if (brush != null) {
                drawRect(brush)
            }
        }
    }
}

@Composable
fun ButtonShader(
    modifier: Modifier,
    shaderSrc: String,
    fallbackColor: Color? = null
) {
    val infiniteTransition = rememberInfiniteTransition("animation")
    val animation = infiniteTransition.animateFloat(
        label = "loop_0_1",
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1300, easing = LinearEasing),
        )
    )
    var error by remember { mutableStateOf(false) }
    if (error.not()) {
        val shader = remember(shaderSrc) { PixelShader(shaderSrc, onErrorFallback = { error = true }) }
        shader.setFloatUniform("iTime", animation.value)
        ComposeGl(modifier = modifier, renderer = shader.renderer)
    } else {
        if (fallbackColor != null) {
            Box(modifier.background(fallbackColor))
        }
    }
}

@Language("AGSL")
@Composable
fun buttonShineShader(nightMode: Boolean = isSystemInDarkTheme()) = """
uniform float2 iResolution;
uniform float iTime;

half4 main(vec2 fragCoord) {
    fragCoord /= iResolution.x; // scale to device width
    float timePos = iTime * 3.0;
    float width = 0.1;
    float pos = fragCoord.x + fragCoord.y * .4;
    float highLight = smoothstep(timePos -width, timePos, pos);
    highLight -= smoothstep(timePos, timePos + width, pos);
    //highLight = max(0.0, highLight);
    vec3 col = vec3(highLight);
    return vec4(col, highLight);
}
"""
