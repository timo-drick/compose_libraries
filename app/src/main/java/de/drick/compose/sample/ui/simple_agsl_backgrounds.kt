package de.drick.compose.sample.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import de.drick.common.log
import de.drick.compose.opengl.ComposeGl
import de.drick.compose.opengl.PixelShader
import org.intellij.lang.annotations.Language

@Language("AGSL")
val shaderCode = """
    float3 color1 = float3(0.1);
    float3 color2 = float3(0.9);
    
    float3 checkerBoard(float2 uv) {
        float2 id = floor(uv);
        float w = fract((id.x + id.y) / 2.0) * 2.0;
        return mix(color1.rgb, color2.rgb, w);
    }
    
    float4 main(float2 fragCoord) {
        return float4(checkerBoard(fragCoord / 100.0), 1);
    }
""".trimIndent()

@RequiresApi(33)
fun RuntimeShader.setColorUniform(uniformName: String, color: Color) =
    setColorUniform(uniformName, color.toArgb())
    //setFloatUniform(uniformName, color.red, color.green, color.blue, color.alpha)

fun Modifier.backgroundShader(
    shaderSrc: String,
    background: Color? = null,
    primary: Color? = null
): Modifier = if (Build.VERSION.SDK_INT >= 33) this.drawWithCache {
    val shader = RuntimeShader(shaderSrc)
    val brush = ShaderBrush(shader)
    if (background != null) {
        shader.setColorUniform("background", background)
    }
    if (primary != null) {
        shader.setColorUniform("primary", primary)
    }
    onDrawBehind {
        drawRect(brush)
    }
} else if (background != null) this.background(background) else this

@Composable
fun BackgroundShader(
    modifier: Modifier = Modifier,
    shaderSrc: String,
    background: Color? = null,
    primary: Color? = null
) {
    if (Build.VERSION.SDK_INT >= 33) {
        Spacer(
            modifier = modifier.drawWithCache {
                val shader = RuntimeShader(shaderSrc)
                val brush = ShaderBrush(shader)
                if (background != null) {
                    shader.setColorUniform("background", background)
                }
                if (primary != null) {
                    shader.setColorUniform("primary", primary)
                }
                onDrawBehind {
                    drawRect(brush)
                }
            }
        )
    } else {
        val shader = remember {
            PixelShader(shaderSrc)
        }
        if (background != null) {
            shader.setColorUniform("background", background)
        }
        if (primary != null) {
            shader.setColorUniform("primary", primary)
        }
        ComposeGl(renderer = shader.renderer, modifier = modifier)
    }
}

@Language("AGSL")
val solidColor = """
    half4 main(float2 fragCoord) {
      return half4(1, 0, 0, 1);
    }
""".trimIndent()

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
fun Modifier.bgScaling(shaderSrc: String) = this.drawWithCache {
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
fun Modifier.bgDensityShader(shaderSrc: String) = composed {
    val width = 400.dp
    val dpPixel = with(LocalDensity.current) { width.toPx() }
    drawWithCache {
        val shader = RuntimeShader(shaderSrc)
        // 10 checkerboard pattern per 400 dp
        val s = 10f / dpPixel
        shader.setFloatUniform("iScaling", s, s)
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
    if (Build.VERSION.SDK_INT >= 33) {
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


@Language("AGSL")
val shaderAOABackground = """
layout(color) uniform vec4 background;
layout(color) uniform vec4 primary;

float PI = 3.14159265359;

float dSine(vec2 uv, vec2 offset, float amplitude) {
    float a = 2.5;
    return uv.y + offset.y - cos((sin(uv.x*a*1.1)*a + PI + offset.x)*1.) * amplitude * 1.6;
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

const float width = 0.01;

vec3 Grid(vec2 uv) {
    vec3 col = vec3(0);
    if(abs(uv.x) < uv.x + width) col.g = 1.;
    if(abs(uv.y) < uv.y + width) col.r = 1.;
    vec2 grid = 1.-abs(fract(uv)-.5)*2.;
    grid = smoothstep(grid + width, vec2(0), grid);
    col += (grid.x+grid.y)*.5;
    return col*.5;
}

float Line(in vec2 p, in vec2 a, in vec2 b, float w) {
    vec2 pa = p - a, ba = b - a;
    float h = clamp(dot(pa,ba) / dot(ba,ba), 0., 1.);
    float d = length(pa - ba * h);
    return smoothstep(w, w-d+width, d);
}

half4 main(vec2 fragcoord) {
    vec2 uv = (fragcoord) / 1100.;
    uv *= vec2(.25, -1.);
    uv *= 1.5;
    float freq = 2.;
    float id = floor(uv.y / freq);
    uv.y = mod(uv.y, freq)-.5*2.;

    float d = 0.0;
    float mask = 0.0;

    uv.x += 5.*id;

    d = max(d, dMainLine(uv, vec2(.0, .0), 0.3)*0.8);
    d = max(d, dMainLine(uv, vec2(-.03, .033), 0.33)*0.6);
    mask = max(mask, dMaskLine(uv, vec2(.0, .0), 0.3)*0.8);
    float bshift = .2;
    d = max(d, dMainLine(uv, vec2(-.2, .10), 0.3)*0.3);
    mask = max(mask, dMaskLine(uv, vec2(-.2, .10), 0.3)*0.3);
    d = max(d, dMainLine(uv, vec2(-.25, .15), 0.34)*0.2);

    //d = clamp(d, .0, 1.);

    vec3 colLine = mix(vec3(1.), primary.rgb, d);
    vec3 col = mix(background.rgb, colLine, mask);
    return vec4(col, 1);
}    
""".trimIndent()