package de.appsonair.compose.live_code_plugin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MainScreen(basePath: String, service: RemoteLiveService) {
    val interfaceList = service.broadcastingInterfaces
    val connectionList = service.connectedDevices

    val serviceIsRunning = service.isRunning

    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            if (serviceIsRunning) {
                Column {
                    Text("Project path: $basePath")
                    ProgressButton(
                        onClick = { service.stopService() }
                    ) {
                        Text("Stop live code service")
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                        onClick = { service.startService(basePath) }
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