package de.appsonair.compose.live_code_plugin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds

@Composable
fun MainScreen() {
    val connectionList = remember { mutableStateListOf<String>() }

    MaterialTheme {
        Column {
            Text("Connected clients and files")
            LazyColumn {
                items(connectionList) { connection ->
                    Text(connection)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val socket = DatagramSocket()
            socket.broadcast = true

            val buffer: ByteArray = "Hello World".toByteArray(Charsets.UTF_8)

            val interfaces = listAllBroadcastAddresses()
            while (isActive) {
                interfaces.forEach { address ->
                    println("Send: $address")
                    val packet = DatagramPacket(buffer, buffer.size, address, 6789)
                    socket.send(packet)
                }
                delay(1000)
            }
            socket.close()
        }

        launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(6789) //TODO dynamic port
            while (isActive) {
                println("Wait for client connection on: ${serverSocket.inetAddress}")
                val clientSocket = serverSocket.accept()
                launch {
                    val connectionText = "${clientSocket.inetAddress} connected"
                    connectionList.add(connectionText)
                    startClientConnection(clientSocket)
                    println("Remove: $connectionText")
                    connectionList.remove(connectionText)
                }
            }
        }
    }

}

fun sendFile(outStream: PrintWriter, file: File) {
    outStream.println("START:${file.absolutePath}")
    outStream.print(file.readText())
    outStream.println("STOP:${file.absolutePath}")
    outStream.flush()
}

suspend fun startClientConnection(tcpSocket: Socket) = withContext(Dispatchers.IO) {
    println("Client connection established: ${tcpSocket.inetAddress}")
    val outStream = PrintWriter(tcpSocket.getOutputStream(), true)
    val inStream = BufferedReader(InputStreamReader(tcpSocket.getInputStream()))
    val firstLine = inStream.readLine()
    println("Received: $firstLine")
    //load code
    val file = File(firstLine)
    println("Send file content")
    sendFile(outStream, file)
    while (tcpSocket.isConnected) {
        println("Wait for file changes")
        val fileChanged = watchFileChange(file)
        if (fileChanged) {
            sendFile(outStream, file)
        } else {
            break
        }
    }
    outStream.flush()
    tcpSocket.close()
}

@Throws(SocketException::class)
fun listAllBroadcastAddresses(): List<InetAddress> =
    NetworkInterface.getNetworkInterfaces().asSequence()
        .filter { it.isLoopback.not() && it.isUp }
        .flatMap { it.interfaceAddresses }
        .mapNotNull { it.broadcast }
        .toList()

fun watchFileChange(file: File): Boolean {
    val watchService = FileSystems.getDefault().newWatchService()
    file.parentFile.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)

    var fileChanged = false
    while (fileChanged.not()) {
        val watchKey = watchService.take()
        for (event in watchKey.pollEvents()) {
            if (event.context().toString() == file.name) fileChanged = true
            println("event: ${event.kind()} context: ${event.context()} ${event.count()}")
        }
        if (!watchKey.reset() || fileChanged) {
            watchKey.cancel()
            watchService.close()
            break
        }
    }
    return fileChanged
}