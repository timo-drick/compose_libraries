package de.appsonair.compose.livecode

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
import kotlinx.coroutines.launch

private const val PROJECT_FOLDER = "/home/timo/projects/compose/github/compose_libraries"

fun main()  {
    val service = RemoteLiveService(PROJECT_FOLDER)
    println("Test file watcher")

    singleWindowApplication {
        MaterialTheme {
            MainScreen(service)
        }
    }
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
                            Text("${address.address} - > ${address.broadcast}")
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