package de.drick.compose.sample.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.drick.compose.sample.R

/**
 * From ShaderToy: https://www.shadertoy.com/view/MdjfRK
 */
const val GlowSource = """
uniform float2 iResolution;          // viewport resolution (in pixels)
uniform float iTime;                 // shader playback time (in seconds)

float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898,12.1414))) * 83758.5453);
}

float noise(vec2 n) {
    const vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n);
    vec2 f = mix(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

vec3 ramp(float t) {
    return t <= .5 ? vec3( 1. - t * 1.4, .2, 1.05 ) / t : vec3( .3 * (1. - t) * 2., .2, 1.05 ) / t;
}

float fire(vec2 n) {
    return noise(n) + noise(n * 2.1) * .6 + noise(n * 5.4) * .42;
}

vec4 main(vec2 fragCoord) {
    
    float t = iTime;
    vec2 uv = fragCoord / iResolution.y;
    
    uv.x += uv.y < .5 ? 23.0 + t * .35 : -11.0 + t * .3;
    uv.y = abs(uv.y - 0.5);
    uv *= 2.5;
    
    float q = fire(uv - t * .013) / 2.0;
    vec2 r = vec2(fire(uv + q / 2.0 + t - uv.x - uv.y), fire(uv + q - t));
    vec3 color = vec3(1.0 / (pow(vec3(0.5, 0.0, .1) + 1.61, vec3(4.0))));
    
    float grad = pow((r.y + r.y) * max(.0, uv.y) + .1, 4.0);
    color = ramp(grad);
    color /= (1.50 + max(vec3(0), color));
    
    float alpha = 1.0-grad;              // <-- you could use the grad value for alpha
    return vec4(color, 1.0);   // <-- premultiply the color. It does not really make a big difference i think.
//    return vec4(color, 0.0);
}
"""

@Composable
fun FlameScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .flameEffect2()
                //.background(Color.White)
        )
    }
}

fun Modifier.flameEffect2() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) this.composed {
    val shader = remember { RuntimeShader(GlowSource) }
    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                value = it / 1000f
            }
        }
    }
    this.drawWithCache {
        val brush = ShaderBrush(shader)
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time)
        onDrawBehind {
            drawRect(brush, blendMode = BlendMode.Screen)
        }
    }
} else this.background(Color.Black)
