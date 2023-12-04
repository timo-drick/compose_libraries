package de.drick.compose.sample.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.drick.compose.sample.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket

// Asset folder inside of the project.
const val ASSET_SRC_FOLDER = "app/src/main/assets"

@Composable
fun remoteAssetAsState(fileName: String): String {
    val ctx = LocalContext.current
    val assetSrc = remember(fileName) {
        ctx.assets.open(fileName).bufferedReader().readText()
    }
    val remoteSrc = remoteSourceAsState("$ASSET_SRC_FOLDER/$fileName")
    return remoteSrc ?: assetSrc
}

@Composable
fun remoteSourceAsState(absFilePath: String): String? {
    if (BuildConfig.DEBUG) {
        var fileContent: String? by remember(absFilePath) { mutableStateOf(null) }
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                while (isActive) {
                    log("Wait for broadcast")
                    val recvPacket = subscriptionFlow.first()//subscriptionFlow.flow.first()
                    log("Connecting")
                    // Establish connection to server
                    Socket(recvPacket.address, 6789).use { tcpSocket ->
                        val outStream = PrintWriter(tcpSocket.getOutputStream(), true)
                        val inStream = BufferedReader(InputStreamReader(tcpSocket.getInputStream()))
                        outStream.println(absFilePath)
                        while (tcpSocket.isConnected) {
                            try {
                                var response = inStream.readLine()
                                //log("Received: $response")
                                if (response == "START:$absFilePath") {
                                    fileContent = buildString {
                                        while (true) {
                                            response = inStream.readLine()
                                            if (response == "STOP:$absFilePath") break
                                            appendLine(response)
                                        }
                                    }
                                    //log("File receiver: $fileContent")
                                }
                                if (response == null) break
                            } catch (err: Throwable) {
                                log(err)
                            }
                        }
                        log("Socket closed")
                        outStream.close()
                        inStream.close()
                    }
                }
                log("Finished")
            }
        }
        return fileContent
    } else {
        return null
    }
}


private val broadcastReceiverFlow = flow<DatagramPacket> {
    val buf = ByteArray(20)
    val recvPacket = DatagramPacket(buf, buf.size)
    while (currentCoroutineContext().isActive) {
        log("Wait for broadcast")
        DatagramSocket(6789).use { s ->
            s.receive(recvPacket)
        }
        log("packet receiver: ${recvPacket.address} -> ${String(recvPacket.data, 0, 11, Charsets.UTF_8)}")
        emit(recvPacket)
    }
}.flowOn(Dispatchers.IO)

val scope = CoroutineScope(Dispatchers.IO)
private val subscriptionFlow = broadcastReceiverFlow.subscriptionFlow(scope)

/**
 * Returns a shared flow which is cold. So it only processes the flow when
 * at least one subscriber is consuming it. And it suspends the flow when no
 * one tries to collect values.
 */
fun <T>Flow<T>.subscriptionFlow(scope: CoroutineScope): SharedFlow<T> {
    val flow = this
    val mutableFlow = MutableSharedFlow<T>()
    scope.launch {
        flow.collect { baseValue ->
            if (mutableFlow.subscriptionCount.value > 0) {
                log("emit value: subcount: ${mutableFlow.subscriptionCount.value}")
                mutableFlow.emit(baseValue)
            } else {
                //Suspend until subscriptionCount > 0
                log("wait for subscription count > 0")
                mutableFlow.subscriptionCount.filter { it > 0 }.first()
                log("Subscription count > 0")
            }
        }
    }
    return mutableFlow
}

fun log(msg: String) {
    Log.d("RemoteLiveCoding", msg)
}