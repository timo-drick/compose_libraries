package de.appsonair.compose.livecode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.InetAddress
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.Path

private const val PROJECT_FOLDER = "/home/timo/github/compose_libraries"

fun main()  {
    val service = RemoteLiveService(PROJECT_FOLDER)
    println("Test file watcher")

    singleWindowApplication {
        MaterialTheme {
            MainScreen(service)
        }
    }
}


@Preview
@Composable
fun PreviewMain() {

}

@Composable
fun MainScreen(service: RemoteLiveService) {
    val interfaceList = service.broadcastingInterfaces
    val connectionList = service.connectedDevices

    val serviceIsRunning = service.isRunning

    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            if (serviceIsRunning) {
                Column {
                    CircularProgressIndicator()
                    Text("Project path: ${service.basePath}")
                    ProgressButton(
                        onClick = { service.stopService() }
                    ) {
                        Text("Stop live code service")
                    }
                    LazyColumn {
                        item {
                            Text("Broadcasting on:")
                        }
                        items(interfaceList) { address ->
                            Text(address.toString())
                        }
                        item {
                            Text("Connected clients and files")
                        }
                        items(connectionList) { device ->
                            Text("${device.name} -> ${device.path}")
                        }
                    }
                }
            } else {
                Column {
                    ProgressButton(
                        onClick = { service.startService() }
                    ) {
                        Text("Start live code service")
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressButton(
    modifier: Modifier = Modifier,
    onClick: suspend () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val inProgress = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box {
        Button(
            modifier = modifier,
            enabled = inProgress.value.not(),
            onClick = {
                inProgress.value = true
                scope.launch {
                    onClick()
                    inProgress.value = false
                }
            },
            content = content
        )
        if (inProgress.value) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}