package de.drick.compose.sample.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import de.drick.common.log
import org.intellij.lang.annotations.Language
import kotlin.math.PI

@Language("AGSL")
private const val shaderSrc = """
uniform float3 iResolution;
uniform float iDensity;
uniform float iTime;

layout(color) uniform half4 background;
layout(color) uniform half4 primary;
uniform vec3 at[2];

float PI = 3.14159265359;

vec2 hash22(vec2 p) {
    p = p * mat2(113.5, 532.1, 269.9, 183.3);
	p = -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
	return sin(p * 6.283 + iTime);
}

float perlin_noise(vec2 p) {
	vec2 pi = floor(p);
    vec2 pf = p - pi;

    vec2 w = pf * pf * (3. - 2. * pf);

    float f00 = dot(hash22(pi + vec2(0.0, 0.0)), pf - vec2(0.0, 0.0));
    float f01 = dot(hash22(pi + vec2(0.0, 1.0)), pf - vec2(0.0, 1.0));
    float f10 = dot(hash22(pi + vec2(1.0, 0.0)), pf - vec2(1.0, 0.0));
    float f11 = dot(hash22(pi + vec2(1.0, 1.0)), pf - vec2(1.0, 1.0));

    float xm1 = mix(f00, f10, w.x);
    float xm2 = mix(f01, f11, w.x);

    float ym = mix(xm1, xm2, w.y);
    return ym;

}

float noise(vec2 p){
    p *= 8.;
	float a = 1., r = 0., s = 0.;
    for (int i=0; i<4; i++) {
      r += a * perlin_noise(p); s += a; p *= 2.; a *= .5;
    }
    return r/s;
}

half4 main(vec2 fragCoord) {
    vec2 uv = fragCoord / (400. * iDensity) ;
    float f = noise(uv);
    return vec4(mix(at[1], at[0], f), 1);
}
"""

fun Modifier.noiseBackground(): Modifier = composed {
    if (Build.VERSION.SDK_INT >= 33) {
        val infiniteTransition = rememberInfiniteTransition()
        val time = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = PI.toFloat() * 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            )
        )
        val density = LocalDensity.current
        val background = Color.DarkGray
        val primary = Color.Gray
        val noiseShader = remember { RuntimeShader(shaderSrc) }
        val brush = remember { ShaderBrush(noiseShader) }

        this.drawWithCache {
            noiseShader.setFloatUniform("iDensity", density.density)
            noiseShader.setFloatUniform("iTime", time.value)
            noiseShader.setColorUniform("background", background.toArgb())
            noiseShader.setColorUniform("primary", primary.toArgb())
            noiseShader.setFloatUniform("at", floatArrayOf(1f,0f,0f, 0f,1f,0f))
            //noiseShader.setFloatUniform("at[1]", 0f, 1f, 0f)


            noiseShader.setFloatUniform("iResolution", size.width, size.height, 1f)
            onDrawBehind {
                drawRect(
                    brush = brush,
                    topLeft = Offset.Zero,
                    size = size
                )
            }
        }
    } else this
}
