package de.drick.compose.sample.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.drick.common.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket

@Composable
fun remoteFileAsState(absFilePath: String): String {
    var fileContent by remember(absFilePath) { mutableStateOf("") }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val buf = ByteArray(20)
            val recvPacket = DatagramPacket(buf, buf.size)
            while (isActive) {
                val s = DatagramSocket(6789)
                log("Wait for broadcast")
                s.receive(recvPacket)
                log("packet receiver: ${recvPacket.address} -> ${String(recvPacket.data, 0, 11, Charsets.UTF_8)}")
                s.close()
                // Establish connection to server
                Socket(recvPacket.address, 6789).use { tcpSocket ->
                    val outStream = PrintWriter(tcpSocket.getOutputStream(), true)
                    val inStream = BufferedReader(InputStreamReader(tcpSocket.getInputStream()))
                    outStream.println(absFilePath)
                    while (tcpSocket.isConnected) {
                        try {
                            var response = inStream.readLine()
                            log("Received: $response")
                            if (response == "START:$absFilePath") {
                                fileContent = buildString {
                                    while (true) {
                                        response = inStream.readLine()
                                        if (response == "STOP:$absFilePath") break
                                        appendLine(response)
                                    }
                                }
                                log("File receiver: $fileContent")
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
}
