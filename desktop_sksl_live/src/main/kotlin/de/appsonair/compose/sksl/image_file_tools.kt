package de.appsonair.compose.sksl

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.use
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import java.io.File

fun main() {
    sampleCreateSKSLImageSequence(
        shaderFile = File("desktop_sksl_live/spinner.glsl"),
        steps = 80,
        outputFolder = File("anim")
    )
}

fun sampleCreateSKSLImageSequence(
    shaderFile: File,
    steps: Int,
    outputFolder: File
) {
    val shader = RuntimeEffect.makeForShader(shaderFile.readText())
    for (i in 0 until steps) {
        val image = drawToImage(shader, i.toFloat() / steps.toFloat(), Size(400f, 400f))
        val skiaBitmap = Image.makeFromBitmap(image.asSkiaBitmap())
        val data =
            skiaBitmap.encodeToData(EncodedImageFormat.WEBP) ?: error("Unable to create webp image")
        println("write frame: $i")
        File(outputFolder, "sequence_$i.webp").writeBytes(data.bytes)
    }
}

fun drawToImage(
    effect: RuntimeEffect,
    iTime: Float = 1f,
    size: Size,
    uniforms: (RuntimeShaderBuilder) -> Unit = {}
): ImageBitmap {
    val density = 2f
    val builder = RuntimeShaderBuilder(effect)
    builder.uniform("iResolution", size.width, size.height)
    builder.uniform("iDensity", density)
    builder.uniform("iTime", iTime)
    uniforms(builder)
    val shader = builder.makeShader()
    val image = ImageBitmap(width = size.width.toInt(), height = size.height.toInt())
    val canvas = Canvas(image)
    val paint = Paint()
    paint.shader = shader
    canvas.drawRect(0f, 0f, size.width, size.height, paint)
    return image
}

fun createImageFromCompose(content: @Composable () -> Unit) {
    val image = ImageComposeScene(
        width = 1000,
        height = 1000,
        content = content
    ).use { scene ->
        scene.render().toComposeImageBitmap()
    }
    saveImage(image, File("test.webp"))
}
fun saveImage(image: ImageBitmap, toFile: File) {
    val skiaBitmap = Image.makeFromBitmap(image.asSkiaBitmap())
    val data =
        skiaBitmap.encodeToData(EncodedImageFormat.WEBP) ?: error("Unable to create webp image")
    toFile.writeBytes(data.bytes)
}
