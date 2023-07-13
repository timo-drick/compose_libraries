package de.drick.compose.sample.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI


@Composable
fun AttitudeArrow(modifier: Modifier) {
    val ctx = LocalContext.current

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
            val shaderSrc = ctx.assets.open("arrow.glsl").bufferedReader().readText()
            RuntimeShader(shaderSrc)
        }
        val brush = remember { ShaderBrush(pieShader) }
        Spacer(modifier
            .drawWithCache {
                pieShader.setFloatUniform("iResolution", size.width, size.height)
                //log("set r: ${rotationVector[0]}, ${rotationVector[1]}, ${rotationVector[2]}")
                val rv = rotationVector.value
                pieShader.setFloatUniform("iLightDir", -rv[1], -rv[2], rv[0])
                pieShader.setFloatUniform("iTime", time.value)

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
    }
}
