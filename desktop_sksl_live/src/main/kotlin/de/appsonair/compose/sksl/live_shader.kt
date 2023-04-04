package de.appsonair.compose.sksl

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds


@Composable
fun rememberLiveEffect(file: File): RuntimeEffect {
    val source = fileAsState(file)
    val runtimeEffect by remember(source) {
        try {
            mutableStateOf(RuntimeEffect.makeForShader(source))
        } catch (err: Throwable) {
            println(err.message)
            err.printStackTrace()
            mutableStateOf(
                RuntimeEffect.makeForShader("""
                half4 main(vec2 fragcoord) {
                    return half4(0);
                }
            """.trimIndent()))
        }
    }
    return runtimeEffect
}


fun Modifier.skslBackground(
    effect: RuntimeEffect,
    uniforms: (RuntimeShaderBuilder) -> Unit = {}
): Modifier = composed {
    val density = LocalDensity.current
    this.drawWithCache {
        val builder = RuntimeShaderBuilder(effect)
        builder.uniform("iResolution", size.width, size.height, 1f)
        builder.uniform("iDensity", density.density)
        uniforms(builder)
        val shader = builder.makeShader()
        val brush = ShaderBrush(shader)
        onDrawBehind {
            drawRect(brush = brush, topLeft = Offset.Zero, size = size)
        }
    }
}

@Composable
fun fileAsState(file: File): String {
    var fileContent by remember(file) { mutableStateOf(file.readText()) }
    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            val watchService = FileSystems.getDefault().newWatchService()
            file.parentFile.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
            while (isActive) {
                val watchKey = watchService.take()
                var fileChanged = false
                for (event in watchKey.pollEvents()) {
                    if (event.context().toString() == file.name) fileChanged = true
                    println("event: ${event.kind()} context: ${event.context()} ${event.count()}")
                }
                if (fileChanged) {
                    println("file changed: $file")
                    fileContent = file.readText()
                }
                if (!watchKey.reset()) {
                    watchKey.cancel()
                    watchService.close()
                    break
                }
            }
        }
    }
    return fileContent
}
