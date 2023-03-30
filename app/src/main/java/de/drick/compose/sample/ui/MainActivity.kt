package de.drick.compose.sample.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import de.drick.common.LogConfig
import de.drick.compose.opengl.ComposeGl
import de.drick.compose.opengl.PixelShader
import de.drick.compose.opengl.simpleFragmentShader
import de.drick.compose.progress_indication.ProgressOverlay
import de.drick.compose.sample.BuildConfig
import de.wurst.formularwizardchallenge.ui.theme.FormularTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogConfig.isActivated = BuildConfig.DEBUG
        setContent {
            FormularTheme {
                MainScreen()
            }
        }
    }
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }

    // A surface container using the 'background' color from the theme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val density = LocalDensity.current.density
        val time by produceState(0) {
            while (true) {
                withInfiniteAnimationFrameMillis {
                    value = it.toInt()
                }
            }
        }
        val pixelShader = remember {
            PixelShader(testFragmentShader)
        }
        pixelShader.setIntUniform("iFrame", time)
        ComposeGl(
            modifier = Modifier.fillMaxSize(),
            renderer = pixelShader.renderer
        )
        ProgressOverlay(isVisible = isLoading, progressIndication = { CircularProgressIndicator() })
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        delay(2000)
                        isLoading = false
                    }
                }
            ) {
                Text("Load")
            }
        }
    }
}

@Language("GLSL")
val shader = """
    precision mediump float;
    
    varying vec2 fragCoord;
    uniform vec2 iResolution; // viewport resolution (in pixels)
    uniform float iDensity;   // pixel density
    uniform int  iFrame;      // shader playback frame
        
    float PI = 3.14159265359;

    //vec3 background = vec3(254./255., 238./255., 210./255.);
    vec3 background = vec3(250./255., 195./255., 103./255.);
    vec3 primary = vec3(244./255., 159./255., 13./255.);
    
    vec3 color(int r, int g, int b) {
        return vec3(float(r) / 255.0, float(g) / 255.0, float(b) / 255.0);
    }
    
    float dSine(vec2 uv, vec2 offset, float amplitude) {
        float a = 2.5;
        return uv.y + offset.y - cos(sin(uv.x*a*1.1)*a + PI + offset.x) * amplitude*1.6;
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
    
    void main() {
        vec2 uv = -1. + 2. * fragCoord;
        uv *= iDensity * vec2(.1, 1);
        
        float d = 0.0;
        float mask = 0.0;
        uv.x += 0.9;
        d = max(d, dMainLine(uv, vec2(.0, .0), 0.3)*0.8);
        d = max(d, dMainLine(uv, vec2(-.03, .033), 0.33)*0.6);
        mask = max(mask, dMaskLine(uv, vec2(.0, .0), 0.3)*0.8);
        float bshift = .2;
        d = max(d, dMainLine(uv, vec2(-.2, .10), 0.3)*0.3);
        mask = max(mask, dMaskLine(uv, vec2(-.2, .10), 0.3)*0.3);
        d = max(d, dMainLine(uv, vec2(-.25, .15), 0.34)*0.2);
        
        //d = clamp(d, .0, 1.);
        
        vec3 colLine = mix(vec3(1.), primary, d);
        vec3 col = mix(background, colLine, mask);
        
        gl_FragColor = vec4(uv, 0., 1.0);
    }

""".trimIndent()
