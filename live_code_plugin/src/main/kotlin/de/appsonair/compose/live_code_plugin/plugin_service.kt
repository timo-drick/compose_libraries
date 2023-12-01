package de.appsonair.compose.live_code_plugin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.net.*


data class ConnectedDevice(
    val name: String,
    val path: String
)

@Service
class RemoteLiveService : Disposable {
    private var remoteServiceJob: Job? = null
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    var isRunning by mutableStateOf(false)
        private set

    private var stopServers = false

    private var basePath = ""

    private val _connectedDevices = mutableStateListOf<ConnectedDevice>()
    val connectedDevices: SnapshotStateList<ConnectedDevice> = _connectedDevices

    private val _broadcastingInterfaces = mutableStateListOf<InterfaceAddress>()
    val broadcastingInterfaces: SnapshotStateList<InterfaceAddress> = _broadcastingInterfaces

    private val fileWatchService = FileWatchService()

    init {
        log("RemoteLiveService initialized")
    }

    suspend fun startService(basePath: String) {
        log("Starting live service")
        remoteServiceJob?.cancel()
        this.basePath = basePath
        stopServers = false
        remoteServiceJob = scope.launch(Dispatchers.IO) {
            val broadcastJob = launch(Dispatchers.IO) { runBroadcast() }
            val tcpServerJob = launch(Dispatchers.IO) { runServer() }
            broadcastJob.join()
            log("Broadcast job finished")
            tcpServerJob.cancel()
            tcpServerJob.join()
            log("tcp server job finished")
        }
        isRunning = true
    }

    suspend fun stopService() = withContext(Dispatchers.IO) {
        log("Stopping live service")
        stopServers = true
        remoteServiceJob?.let { job ->
            job.join()
            fileWatchService.stopAll()
            log("Service stopped")
            isRunning = false
        }
    }

    private suspend fun runBroadcast() = withContext(Dispatchers.IO) {
        try {
            DatagramSocket().use { socket ->
                socket.broadcast = true

                val buffer: ByteArray = "Hello World".toByteArray(Charsets.UTF_8)

                val interfaces = listAllBroadcastAddresses()
                log("Detected interfaces:")
                interfaces.forEach { log(it.address) }
                _broadcastingInterfaces.clear()
                _broadcastingInterfaces.addAll(interfaces)
                while (isActive && stopServers.not()) {
                    interfaces.forEach { interfaceAddress ->
                        val broadcast = interfaceAddress.broadcast
                        val packet = DatagramPacket(buffer, buffer.size, broadcast, 6789)
                        socket.send(packet)
                    }
                    delay(1000)
                }
                broadcastingInterfaces.clear()
            }
        } catch (err: Throwable) {
            log(err)
        }
    }

    private suspend fun runServer() = withContext(Dispatchers.IO) {
        log("Create server")
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().bind("0.0.0.0", 6789)
        socket.use { serverSocket ->
            try {
                log("Server listening on: ${serverSocket.localAddress}")
                while (stopServers.not()) {
                    log("Wait for client connection on: ${serverSocket.localAddress}")
                    val clientSocket = serverSocket.accept()
                    launch(Dispatchers.IO) {
                        var connectedDevice: ConnectedDevice? = null
                        try {
                            clientSocket.use { clientSocket ->
                                log("Client connection established: ${clientSocket.localAddress}")
                                startClientConnection(clientSocket, established = { path ->
                                    connectedDevice = ConnectedDevice(
                                        name = clientSocket.remoteAddress.toString(),
                                        path = path
                                    ).also {
                                        _connectedDevices.add(it)
                                    }
                                })
                            }
                        } catch (err: Throwable) {
                            log(err)
                        } finally {
                            connectedDevice?.let { _connectedDevices.remove(it) }
                        }
                    }
                }
                log("Server listening on: ${serverSocket.localAddress} closed")
            } catch (err: Throwable) {
                log(err)
            }
        }
        log("Server closed")
    }

    private suspend fun CoroutineScope.startClientConnection(
        tcpSocket: io.ktor.network.sockets.Socket,
        established: (file: String) -> Unit
    ) {
        val outStream = tcpSocket.openWriteChannel()
        val inStream = tcpSocket.openReadChannel()
        val firstLine = inStream.readUTF8Line()
        log("Received: $firstLine")
        val file = firstLine?.let { File(basePath, it) }
        if (file != null) {
            established(firstLine)
            //load code
            log("Send file content")
            sendFile(outStream, firstLine, file.readText())
            val flow = fileWatchService.getMonitor(file)
            flow.collect { newContent ->
                if (isActive) {
                    sendFile(outStream, firstLine, newContent)
                }
            }
        }
    }

    override fun dispose() {
        log("Dispose service")
        runBlocking {
            stopService()
        }
    }
}


suspend fun sendFile(outStream: ByteWriteChannel, name: String, content: String) {
    outStream.writeStringUtf8("START:$name\n")
    outStream.writeStringUtf8(content)
    outStream.writeStringUtf8("\nSTOP:$name\n")
    outStream.flush()
}


@Throws(SocketException::class)
fun listAllBroadcastAddresses(): List<InterfaceAddress> =
    NetworkInterface.getNetworkInterfaces().asSequence()
        .filter { it.isLoopback.not() && it.isUp }
        .flatMap { it.interfaceAddresses }
        .filter { it.broadcast != null }
        .toList()
