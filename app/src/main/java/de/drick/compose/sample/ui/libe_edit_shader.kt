package de.drick.compose.sample.ui

import android.annotation.TargetApi
import android.graphics.RuntimeShader
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.drick.compose.sample.theme.SampleTheme
import org.intellij.lang.annotations.Language

@Preview(device = "spec:id=reference_phone,shape=Normal,width=411,height=891,unit=dp,dpi=420")
@Composable
private fun previewShader() {
    SampleTheme {
        LiveEditShaderScreen(
            modifier = Modifier.fillMaxSize(),
            padding = PaddingValues(0.dp)
        )
    }
}

@TargetApi(33)
@Composable
fun LiveEditShaderScreen(
    padding: PaddingValues,
    modifier: Modifier = Modifier
) {
    @Language("AGSL")
    val shaderCode = """
    uniform float2 iResolution;
    uniform float iTime;
    const float4 fg = float4(0,0,0,1);
    const float4 bg = float4(0);
    float4 main(float2 fragCoord) {
        fragCoord = fragCoord / iResolution * 2.0 - 1.0;
        float r = 0.3 * sin(iTime * 3.14 * 2.0) + 0.3;
        float d = length(fragCoord)-r;
        return mix(fg, bg, smoothstep(0.0, 0.4 * sin(iTime * 3.14), d));
    }
    """
    Box(
        modifier = modifier
            .liveEditShader(shaderCode)
            .padding(padding)
    ) {
        Button(onClick = { /*TODO*/ }) {
            Text("Hello World")
        }
    }
}

@RequiresApi(33)
fun Modifier.liveEditShader(shaderCode: String): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition("animation")
    val animation = infiniteTransition.animateFloat(
        label = "loop_0_1",
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = LinearEasing),
        )
    )
    val shader: RuntimeShader? = remember(shaderCode) {
        try {
            RuntimeShader(shaderCode)
        } catch (err: Throwable) {
            de.drick.common.log(err)
            null
        }
    }
    drawWithCache {
        val brush = shader?.let { shader ->
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", animation.value)
            ShaderBrush(shader)
        }
        onDrawBehind {
            if (brush != null) {
                drawRect(brush)
            }
        }
    }
}
