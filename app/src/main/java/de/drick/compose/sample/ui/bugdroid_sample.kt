package de.drick.compose.sample.ui

import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import de.drick.compose.opengl.ComposeGl
import de.drick.compose.opengl.PixelShader
import org.intellij.lang.annotations.Language

enum class TextureShader {
    SOLID, LGPT
}

@Composable
fun BugDroidView(modifier: Modifier) {
    val ctx = LocalContext.current
    val density = LocalDensity.current
    val background = Color.LightGray
    val primary = Color.Gray

    val rotationVector = rememberRotationVector()

    val bugdroidSrc = rememberAssetString("shader/android_logo.agsl")
    val textureSolidSrc = rememberAssetString("shader/logo_solid_color.agsl")
    val textureRainbowSrc = rememberAssetString("shader/logo_texture_rainbow.agsl")
    val textureLGPTSrc = rememberAssetString("shader/logo_texture_lgpt.agsl")

    if (Build.VERSION.SDK_INT >= 33) {
        var texture by remember {
            mutableStateOf(TextureShader.SOLID)
        }
        val infiniteTransition = rememberInfiniteTransition("infinite timer")
        val time = infiniteTransition.animateFloat(
            label = "shader time",
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            )
        )
        val bugdroidShader = remember(bugdroidSrc) {
            RuntimeShader(bugdroidSrc)
        }

        val textureShader: Shader = remember(texture) {
            val src = when (texture) {
                TextureShader.SOLID -> textureSolidSrc
                //TextureShader.RAINBOW -> textureRainbowSrc
                TextureShader.LGPT -> textureLGPTSrc
            }
            RuntimeShader(src)
        }
        /*val multisampleShader = remember {
            RuntimeShader(multisampleSrc)
        }*/
        val brush = remember { ShaderBrush(bugdroidShader) }
        //val brushMultisample = remember { ShaderBrush(multisampleShader) }
        Spacer(modifier
            .clickable {
                var textureCounter = texture.ordinal + 1
                if (textureCounter >= TextureShader.entries.size)
                    textureCounter = 0
                texture = TextureShader.entries[textureCounter]
            }
            .drawWithCache {
                bugdroidShader.setFloatUniform("iResolution", size.width, size.height)
                bugdroidShader.setFloatUniform("iTime", time.value)
                bugdroidShader.setColorUniform("fg", Color.Green)
                //log("set r: ${rotationVector[0]}, ${rotationVector[1]}, ${rotationVector[2]}")
                val rv = rotationVector.value
                bugdroidShader.setFloatUniform("iLightDir", -rv[1], -rv[2], rv[0])
                bugdroidShader.setInputShader("texture", textureShader)

                //noiseShader.setFloatUniform("at[1]", 0f, 1f, 0f)

                //multisampleShader.setInputShader("base", bugdroidShader)

                onDrawBehind {
                    drawRect(
                        brush = brush,
                        topLeft = Offset.Zero,
                        size = size
                    )
                }
            }
        )
    } else {
        val pieShader = remember(bugdroidSrc) { PixelShader(bugdroidSrc) }
        val rv = rotationVector.value
        de.drick.common.log("set r: ${rv[0]}, ${rv[1]}, ${rv[2]}")
        pieShader.setFloatUniform("iLightDir", rv[0], rv[1], rv[2])
        pieShader.setColorUniform("background", background)
        //TODO supporting texture shader
        ComposeGl(
            modifier = modifier,
            renderer = pieShader.renderer
        )

    }
}

@Language("AGSL")
private const val multisampleSrc = """
uniform shader base;

const float AA = 2.;

vec4 main(vec2 fragcoord) {
    vec4 col = vec4(0);
    for (float i=0; i<AA; i++) {
        for (float j=0; j<AA; j++) {
            vec2 o = vec2(i,j) / AA - 0.5;
            vec2 uv = (fragcoord + o);
            col += base.eval(uv);
        }
    }
    col /= AA * AA;
    return col;
}
"""