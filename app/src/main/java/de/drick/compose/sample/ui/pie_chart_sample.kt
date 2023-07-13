package de.drick.compose.sample.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import de.drick.common.log
import de.drick.compose.opengl.ComposeGl
import de.drick.compose.opengl.PixelShader
import kotlinx.coroutines.delay
import org.intellij.lang.annotations.Language
import kotlin.math.PI

data class PieData(
    val color: Color,
    val percent: Float,
    val selected: Boolean
)


@Composable
fun PieChart(modifier: Modifier) {
    val testData = listOf(
        PieData(Color.Red, 0.2f, false),
        PieData(Color.Gray, 0.1f, true),
        PieData(Color.Green, 0.3f, false),
        PieData(Color.Magenta, 0.4f, false),
    )
    val ctx = LocalContext.current
    var selected by remember { mutableStateOf(false) }
    val selectionAnimation = animateFloatAsState(targetValue = if (selected) 1f else 0f, animationSpec = tween(500))
    var started by remember { mutableStateOf(true) }
    val startedAnimation = animateFloatAsState(targetValue = if (started) 0f else 1f, animationSpec = tween(2000))
    LaunchedEffect(Unit) {
        delay(100)
        started = false
    }
    val density = LocalDensity.current
    val background = Color.LightGray
    val primary = Color.Gray

    val rotationVector = rememberRotationVector()

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
        val pieShader = remember {
            val shaderSrc = ctx.assets.open("test.glsl").bufferedReader().readText()
            RuntimeShader(shaderSrc)
        }
        val multisampleShader = remember {
            RuntimeShader(multisampleSrc)
        }
        val brush = remember { ShaderBrush(pieShader) }
        val brushMultisample = remember { ShaderBrush(multisampleShader) }
        Spacer(modifier
            .drawWithCache {
                pieShader.setFloatUniform("iResolution", size.width, size.height)
                pieShader.setColorUniform("background", background.toArgb())
                //log("set r: ${rotationVector[0]}, ${rotationVector[1]}, ${rotationVector[2]}")
                val rv = rotationVector.value
                pieShader.setFloatUniform("iLightDir", -rv[1], -rv[2], rv[0])
                pieShader.setIntUniform("iPieces", testData.size)
                pieShader.setFloatUniform("iTime", time.value)
                val maxPieces = 4
                val tmp = testData.toMutableList()
                while (tmp.size < maxPieces) tmp.add(PieData(Color.Black, 0f, false))
                val colorArray = tmp
                    .flatMap { listOf(it.color.red, it.color.green, it.color.blue) }
                    .toFloatArray()
                pieShader.setFloatUniform("color", colorArray)
                val percentArray = tmp
                    .map { it.percent * PI.toFloat() * 2f * startedAnimation.value }
                    .toFloatArray()
                pieShader.setFloatUniform("arc", percentArray)
                val translateArray = tmp
                    .flatMap {
                        listOf(
                            0f,
                            if (it.selected) -0.2f * selectionAnimation.value else 0.0f,
                            if (it.selected) 0.3f * selectionAnimation.value else 0f
                        )
                    }
                    .toFloatArray()
                pieShader.setFloatUniform("translate", translateArray)
                //noiseShader.setFloatUniform("at[1]", 0f, 1f, 0f)

                multisampleShader.setInputShader("base", pieShader)

                onDrawBehind {
                    drawRect(
                        brush = brush,
                        topLeft = Offset.Zero,
                        size = size
                    )
                }
            }
            .clickable(onClick = { selected = selected.not() },
                indication = null,
                interactionSource = remember { MutableInteractionSource() })
        )
    } else {
        val pieShader = remember {
            val shaderSrc = ctx.assets.open("test.glsl").bufferedReader().readText()
            PixelShader(shaderSrc)
        }
        val rv = rotationVector.value
        log("set r: ${rv[0]}, ${rv[1]}, ${rv[2]}")
        pieShader.setFloatUniform("iLightDir", rv[0], rv[1], rv[2])
        pieShader.setColorUniform("background", background)
        pieShader.setIntUniform("iPieces", testData.size)
        val maxPieces = 4
        val tmp = testData.toMutableList()
        while (tmp.size < maxPieces) tmp.add(PieData(Color.Black, 0f, false))
        val percentArray = tmp
            .map { it.percent * PI.toFloat() * 2f * startedAnimation.value }
            .toFloatArray()
        pieShader.setFloatUniform1f("arc", percentArray)
        val colorArray = tmp
            .flatMap {
                listOf(it.color.red, it.color.green, it.color.blue)
            }
            .toFloatArray()
        pieShader.setFloatUniform3f("color", colorArray)
        val translateArray = tmp
            .flatMap {
                listOf(
                    0f,
                    if (it.selected) -0.2f * selectionAnimation.value else 0.0f,
                    if (it.selected) 0.3f * selectionAnimation.value else 0f
                )
            }
            .toFloatArray()
        pieShader.setFloatUniform3f("translate", translateArray)
        ComposeGl(
            modifier = modifier.clickable(
                onClick = { selected = selected.not() },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
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