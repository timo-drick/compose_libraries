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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun PixelShaderSamples() {
    Column(Modifier.fillMaxSize()) {
        PixelShaderSample1(modifier = Modifier
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

@Composable
fun PixelShaderSample1(modifier: Modifier) {
    val pixelShader = remember {
        PixelShader("""
    varying vec2  fragCoord;
    void main() {
      gl_FragColor = vec4(1,0,0,1);
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
    uniform vec4 iColor;
    varying vec2 fragCoord;
    void main() {
      gl_FragColor = iColor;
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
    uniform vec2  iResolution; // viewport resolution (in pixels)
    varying vec2 fragCoord;
    void main() {
        vec2 uv = fragCoord * iResolution.x/iResolution.y;
        gl_FragColor = vec4(uv, 0, 1);
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
    val animaton = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = DURATION,
        animationSpec = infiniteRepeatable(
            animation = tween(DURATION.toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        )
    )

    val pixelShader = remember {
        PixelShader("""
    uniform vec2  iResolution; // viewport resolution (in pixels)
    uniform float iTime;
    uniform float iDuration;
    
    varying vec2 fragCoord;
    void main() {
        vec2 uv = fragCoord * iResolution.x/iResolution.y;
        vec2 scaled = abs(1.0-mod(uv+iTime/(iDuration/2.0),2.0));
        gl_FragColor = vec4(scaled, 0, 1);
    }
""")
    }
    pixelShader.setFloatUniform("iDuration", DURATION)
    pixelShader.setFloatUniform("iTime", animaton.value)
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