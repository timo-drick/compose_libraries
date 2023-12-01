package de.appsonair.compose.live_code_plugin

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.util.concurrent.TimeUnit

/**
 * Just for testing purposes
 */
fun main() {
    val service = FileWatchService()
    runBlocking {
        println("Start monitoring files")
        val monitor1 = service.getMonitor(File("/home/timo/github/compose_libraries/live_code_plugin/src/main/resources/test.txt"))
        val monitor2 = service.getMonitor(File("/home/timo/github/compose_libraries/live_code_plugin/src/main/resources/test2.txt"))

        launch {
            monitor1.collect { println("F1 changed") }
            println("Coroutine finished")
        }
        launch { monitor2.collect { println("F2 changed") } }

        delay(5000)
        service.stopAll()
        val m1 = service.getMonitor(File("/home/timo/github/compose_libraries/live_code_plugin/src/main/resources/test.txt"))
        val m2 = service.getMonitor(File("/home/timo/github/compose_libraries/live_code_plugin/src/main/resources/test2.txt"))
        launch { m1.collect { println("F1 changed") } }
        launch { m2.collect { println("F2 changed") } }
        println("Restarted")
        delay(10000)
        service.stopAll()
        delay(1000)
        println("Stopped")
    }
}

class FileWatchService() {
    private var job = Job()
    private var scope = CoroutineScope(job + Dispatchers.IO)
    private val folderMap = mutableMapOf<String, StateFlow<String>>()

    suspend fun getMonitor(file: File): Flow<String> {
        return folderMap.getOrPut(file.absolutePath) {
            textFileAsFlow(file).stateIn(scope)
        }
    }

    suspend fun stopAll() {
        println("Stop service")
        job.cancel()
        job.join()
        folderMap.clear()
        println("Service stopped")
        job = Job()
        scope = CoroutineScope(job + Dispatchers.IO)
    }

    private fun textFileAsFlow(file: File) = flow<String> {
        log("start monitoring file: $file")
        emit(file.readText())
        val watchService = FileSystems.getDefault().newWatchService()
        file.parentFile.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        while (currentCoroutineContext().isActive) {
            var watchKey: WatchKey?
            do {
                watchKey = watchService.poll(500, TimeUnit.MILLISECONDS)
            } while (currentCoroutineContext().isActive && watchKey == null)
            if (watchKey == null) break // We stop if coroutine was cancelled

            var fileChanged = false
            for (event in watchKey.pollEvents()) {
                if (event.context().toString() == file.name) fileChanged = true
                //println("event: ${event.kind()} context: ${event.context()} ${event.count()}")
            }
            if (fileChanged) {
                emit(file.readText())
            }
            if (!watchKey.reset()) {
                watchKey.cancel()
                watchService.close()
                break
            }
        }
        log("stop monitoring file: $file")
    }.flowOn(Dispatchers.IO)

}
