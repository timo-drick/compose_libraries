package de.appsonair.compose.sksl

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import java.io.File
import kotlin.math.roundToInt

/*fun main() {
    val time = 1400f
    val steps = (time / 30f).roundToInt()
    sampleCreateSKSLImageSequence(
        shaderFile = File("desktop_sksl_live/test.glsl"),
        steps = steps,
        outputFolder = File("/home/timo/gitlab/blog/agsl/animation"),
        fileName = "shine_button_compose"
    )
}*/

fun main() {
    val time = 2000f
    val steps = (time / 30f).roundToInt()
    createComposeAnimation(
        steps = steps,
        outputFolder = File("/home/timo/gitlab/blog/agsl/animation"),
        fileName = "shine_button_compose"
    ) {
        AppTheme {
            ShinyButton(onClick = {}, modifier = Modifier.padding(16.dp)) {
                Text("Shiny button test")
            }
        }
    }
}

fun createComposeAnimation(
    steps: Int,
    outputFolder: File,
    fileName: String,
    content: @Composable () -> Unit
) {
    val sampleImage = ImageComposeScene(
        width = 177+18,
        height = 80,
        content = content
    ).use { scene ->
        val sampleImage = scene.render(System.nanoTime()).toComposeImageBitmap()
        saveImage(sampleImage, File(outputFolder, "$fileName.webp"))
        sampleImage
    }
    ImageComposeScene(
        width = 177+18,
        height = 80,
        content = content
    ).use { scene ->
        createGifAnimation(
            file = File(outputFolder, "$fileName.gif"),
            sampleImage = sampleImage,
            delayTicks = 3, // in my tests it looks like this value must be at least 3 otherwise the browser will play it back very slow
            loop = true
        ) {
            for (i in 0 until steps) {
                add(scene.render(System.nanoTime()).toComposeImageBitmap())
            }
        }
    }
}

fun sampleCreateSKSLImageSequence(
    shaderFile: File,
    steps: Int,
    outputFolder: File,
    fileName: String
) {
    val backgroundColor = Color.Black//Color(0xFFF3F6F8)
    val size = Size(512f, 128f)
    val shader = RuntimeEffect.makeForShader(shaderFile.readText())
    val sampleImage = drawToImage(shader, 10.toFloat() / steps.toFloat(), size)
    val withBackground = drawImageToBackground(
        image = sampleImage,
        border = Size(64f, 64f),
        color = backgroundColor
    )
    saveImage(withBackground, File(outputFolder, "$fileName.webp"))
    createGifAnimation(
        file = File(outputFolder, "$fileName.gif"),
        sampleImage = sampleImage,
        delayTicks = 3, // in my tests it looks like this value must be at least 3 otherwise the browser will play it back very slow
        loop = true
    ) {
        for (i in 0 until steps) {
            val image = drawToImage(shader, i.toFloat() / steps.toFloat(), size)
            val withBackground = drawImageToBackground(
                image = image,
                border = Size(64f, 64f),
                color = backgroundColor
            )
            add(withBackground)
        }
    }
    /*for (i in 0 until steps) {
        val image = drawToImage(shader, i.toFloat() / steps.toFloat(), Size(400f, 400f))
        val skiaBitmap = Image.makeFromBitmap(image.asSkiaBitmap())
        val data =
            skiaBitmap.encodeToData(EncodedImageFormat.WEBP) ?: error("Unable to create webp image")
        println("write frame: $i")
        File(outputFolder, "sequence_$i.webp").writeBytes(data.bytes)
    }*/
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

fun drawImageToBackground(
    image: ImageBitmap,
    border: Size,
    color: Color
): ImageBitmap {
    val background = ImageBitmap(
        width = image.width + border.width.toInt() * 2,
        height = image.height + border.height.toInt() * 2
    )
    val canvas = Canvas(background)
    val paint = Paint()
    paint.color = color
    paint.style = PaintingStyle.Fill
    canvas.drawRect(
        left = 0f,
        top = 0f,
        right = background.width.toFloat(),
        bottom = background.height.toFloat(),
        paint = paint
    )
    canvas.drawImage(
        topLeftOffset = Offset(border.width, border.height),
        image = image,
        paint = Paint()
    )
    return background
}

fun createImageFromCompose(content: @Composable () -> Unit): ImageBitmap {
    return ImageComposeScene(
        width = 1000,
        height = 1000,
        content = content
    ).use { scene ->
        scene.render(System.nanoTime()).toComposeImageBitmap()
    }
    //saveImage(image, File("test.webp"))
}
fun saveImage(image: ImageBitmap, toFile: File) {
    val skiaBitmap = Image.makeFromBitmap(image.asSkiaBitmap())
    val data =
        skiaBitmap.encodeToData(EncodedImageFormat.WEBP) ?: error("Unable to create webp image")
    toFile.writeBytes(data.bytes)
}
