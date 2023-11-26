package de.appsonair.compose

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val logFileName = "log.kt"

fun log(msg: Any, error: Throwable? = null) {
    val ct = Thread.currentThread()
    val tname = ct.name
    val traces = ct.stackTrace
    val max = traces.size-1
    val stackTrace = traces.slice(3..max).find { it.fileName != logFileName }
    val message = if (stackTrace != null) {
        val cname = stackTrace.className.substringAfterLast(".")
        "[${stackTrace.fileName}:${stackTrace.lineNumber}] $cname.${stackTrace.methodName} : $msg"
    } else {
        "$msg"
    }
    val dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    println("$dateTime $tname: $message")
    error?.printStackTrace()
}
