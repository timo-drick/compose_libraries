package de.appsonair.compose.sksl

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import org.intellij.lang.annotations.Language
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

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
        Box(
            modifier = Modifier
                .defaultMinSize(
                    minWidth = ButtonDefaults.MinWidth,
                    minHeight = ButtonDefaults.MinHeight
                )
                .shaderAnimation(shaderShinyAnimation)
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

@Language("AGSL")
private val shaderShinyAnimation = """
    uniform float2 iResolution;
    uniform float iTime;
    
    vec4 main(vec2 fragCoord) {
        fragCoord /= iResolution.x;
        float timePos = iTime * 3.;
        float pos = fragCoord.x + fragCoord.y * .4;
        float intensity = smoothstep(timePos - .2, timePos - .1, pos);
        intensity -= smoothstep(timePos - .1, timePos, pos);
        return vec4(intensity);
    }
""".trimIndent()

@Composable
fun Modifier.shaderAnimation(shaderSrc: String) = composed {
    val infiniteTransition = rememberInfiniteTransition("loop")
    val animation = infiniteTransition.animateFloat(
        label = "progress",
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = LinearEasing),
        )
    )
    val effect = remember(shaderSrc) { RuntimeEffect.makeForShader(shaderSrc) }
    this.drawWithCache {
        val shader = RuntimeShaderBuilder(effect).apply {
            println("Shader uniform iTime: ${animation.value}")
            uniform("iTime", animation.value)
            uniform("iResolution", size.width, size.height)
        }.makeShader()
        val brush = ShaderBrush(shader)
        onDrawBehind {
            drawRect(brush)
        }
    }
}
