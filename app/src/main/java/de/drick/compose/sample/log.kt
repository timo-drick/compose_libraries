package de.drick.compose.sample

import android.annotation.SuppressLint
import android.util.Log

const val logFileName = "log.kt"

inline fun log(error: Throwable? = null, msg: () -> String) {
    if (BuildConfig.DEBUG) log(msg(), error)
}

@SuppressLint("LogNotTimber")
fun log(msg: Any, error: Throwable? = null) {
    if (BuildConfig.DEBUG.not()) return
    val ct = Thread.currentThread()
    val tName = ct.name
    val traces = ct.stackTrace
    val max = traces.size - 1
    val stackTrace = traces.slice(3..max).find { it.fileName != logFileName }
    val message = if (stackTrace != null) {
        val cname = stackTrace.className.substringAfterLast(".")
        "[${stackTrace.fileName}:${stackTrace.lineNumber}] $cname.${stackTrace.methodName} : $msg"
    } else {
        "$msg"
    }
    if (error != null) {
        Log.e(tName, message, error)
    } else {
        Log.d(tName, message)
    }
}