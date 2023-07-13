package de.drick.compose.sample.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.drick.compose.opengl.ComposeGl
import de.drick.compose.opengl.PixelShader
import org.intellij.lang.annotations.Language

@Composable
fun ShaderAnimation(modifier: Modifier) {
    // declare the ValueAnimator
    val animationDuration = 10000
    val infiniteTransition = rememberInfiniteTransition()
    val animation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        )
    )

    val pixelShader = remember {
        PixelShader(SHADER_SRC)
    }
    pixelShader.setFloatUniform("iTime", animation.value)
    ComposeGl(
        modifier = modifier,
        renderer = pixelShader.renderer
    )
}

@Language("AGSL")
private const val SHADER_SRC = """
uniform float2 iResolution;
uniform float iTime;

const float PI = 3.14159265359;

mat2 rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}

float sdBox(vec2 p, vec2 b) {
    vec2 d = abs(p)-b;
    return length(max(d,0.0)) + min(max(d.x,d.y),0.0);
}

half4 main(vec2 fragCoord) {
    vec2 centerPos = vec2(100,100);
    fragCoord -= centerPos; // Move 0,0 to center
    float angleRad = PI * 2.0 * iTime;
    fragCoord *= rot(angleRad); // rotate 45 degree
    fragCoord += centerPos; // Move 0,0 to left, top
    float d = sdBox(fragCoord - centerPos, vec2(100, 30));
    return vec4(vec3(d),1);
}
"""