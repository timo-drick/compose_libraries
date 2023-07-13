package de.drick.compose.opengl

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.intellij.lang.annotations.Language

@Composable
fun PixelShaderSamples() {
    Column(Modifier.fillMaxSize()) {
        BasicText(text = "AGSL Shaders run on opengl")
        PixelShaderSampleAGSL(
            Modifier
                .weight(1f)
                .fillMaxWidth())
        PixelShaderSample1(
            Modifier
                .weight(1f)
                .fillMaxWidth())
        PixelShaderSample2ColorUniform(modifier = Modifier
            .weight(1f)
            .fillMaxWidth())
        PixelShaderSample3Gradient(modifier = Modifier
            .weight(1f)
            .fillMaxWidth())
        PixelShaderSample4Animation(modifier = Modifier
            .weight(1f)
            .fillMaxWidth())
    }
}

@Language("AGSL")
val agslSampleShader = """
    uniform vec2 iResolution;
    
    const half4 backgroundColor = half4(1);
    const half4 originColor = half4(0.75,0.75,0.75,1);
    const half4 xColor = half4(1.0,0.84,0.0,1);
    const half4 yColor = half4(0.0,0.34,0.72,1);
    const half4 gridColor = half4(0,0,0,1);

    half4 main(in vec2 fragCoord) {
        // ten grids across on shortest side
        float pitch = min(iResolution.x/10.0, iResolution.y/10.0);
        if (int(mod(fragCoord.x, pitch)) == 0 ||
            int(mod(fragCoord.y, pitch)) == 0) {
              return gridColor;        
        } else {
            float gridY = fragCoord.x/pitch;
            float gridX = fragCoord.y/pitch;
            if (gridX < 1.0 && gridY < 1.0)
              return originColor;
            else if (gridX < 1.0)
              return xColor;
            else if (gridY < 1.0)
              return yColor;
            else
              return backgroundColor;
        }
    }
""".trimIndent()

@Composable
fun PixelShaderSampleAGSL(modifier: Modifier) {
    @Language("AGSL")
    val pixelShader = remember {
        PixelShader(agslSampleShader)
    }
    ComposeGl(
        modifier = modifier,
        renderer = pixelShader.renderer
    )
}
@Composable
fun PixelShaderSample1(modifier: Modifier) {
    val pixelShader = remember {
        PixelShader("""
    half4 main(float2 fragCoord) {
      return half4(1,0,0,1);
   }
""")
    }
    ComposeGl(
        modifier = modifier,
        renderer = pixelShader.renderer
    )
}

@Composable
fun PixelShaderSample2ColorUniform(modifier: Modifier) {
    val pixelShader = remember {
        PixelShader("""
    layout(color) uniform half4 iColor;
   half4 main(float2 fragCoord) {
      return iColor;
   }
""")
    }
    pixelShader.setColorUniform("iColor", Color.Green)
    ComposeGl(
        modifier = modifier,
        renderer = pixelShader.renderer
    )
}

@Composable
fun PixelShaderSample3Gradient(modifier: Modifier) {
    val pixelShader = remember {
        PixelShader("""
    uniform float2 iResolution;
    half4 main(float2 fragCoord) {
      float2 scaled = fragCoord/iResolution.xy;
      return half4(scaled, 0, 1);
    }
""")
    }
    pixelShader.setColorUniform("iColor", Color.Green)
    ComposeGl(
        modifier = modifier,
        renderer = pixelShader.renderer
    )
}

@Composable
fun PixelShaderSample4Animation(modifier: Modifier) {
    // declare the ValueAnimator
    val DURATION = 4000f
    val infiniteTransition = rememberInfiniteTransition()
    val animation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = DURATION,
        animationSpec = infiniteRepeatable(
            animation = tween(DURATION.toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        )
    )

    val pixelShader = remember {
        PixelShader("""
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iDuration;
        half4 main(in float2 fragCoord) {
            float2 scaled = abs(1.0-mod(fragCoord/iResolution.xy+iTime/(iDuration/2.0),2.0));
            return half4(scaled, 0, 1.0);
        }
""")
    }
    pixelShader.setFloatUniform("iDuration", DURATION)
    pixelShader.setFloatUniform("iTime", animation.value)
    ComposeGl(
        modifier = modifier,
        renderer = pixelShader.renderer
    )
}

val testFragmentShader = """
    precision mediump float;
    
    varying vec2  fragCoord;   // pixel coordinates
    uniform vec2  iResolution; // viewport resolution (in pixels)
    uniform float iDensity;    // pixel density
    uniform int   iFrame;      // shader playback frame
    
    void main() {
        // Normalized pixel coordinates (from 0 to 1)
        vec2 uv = fragCoord/iResolution.xy;
    
        // Time varying pixel color
        vec3 col = 0.5 + 0.5*cos(float(iFrame)/1000.0+uv.xyx+vec3(0,2,4));
    
        // Output to screen
        gl_FragColor = vec4(col,1.0);
    }
""".trimIndent()