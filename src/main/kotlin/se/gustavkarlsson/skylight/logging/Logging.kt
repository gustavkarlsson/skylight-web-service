package se.gustavkarlsson.skylight.logging

private val loggers = mutableListOf<Logger>()

fun addLogger(logger: Logger) {
    loggers += logger
}

fun logError(throwable: Throwable? = null, message: () -> String) {
    val stackTrace = getStackTrace()
    loggers.log { error(message, stackTrace, throwable) }
}

fun logWarn(throwable: Throwable? = null, message: () -> String) {
    val stackTrace = getStackTrace()
    loggers.log { warn(message, stackTrace, throwable) }
}

fun logInfo(message: () -> String) {
    val stackTrace = getStackTrace()
    loggers.log { info(message, stackTrace) }
}

fun logDebug(message: () -> String) {
    val stackTrace = getStackTrace()
    loggers.log { debug(message, stackTrace) }
}

private fun Iterable<Logger>.log(block: Logger.() -> Unit) {
    forEach(block)
}

private fun getStackTrace(): Array<StackTraceElement> {
    val stackTrace = Thread.currentThread().stackTrace.drop(1) // Drop the actual call to // Thread.currentThread()
    val thisFileName = stackTrace.first().fileName
    val thisFileFrameCount = stackTrace
        .map { it.fileName }
        .indexOfFirst { it != thisFileName }
    return stackTrace.drop(thisFileFrameCount)
}

private fun Array<StackTraceElement>.drop(n: Int) = copyOfRange(n, size)
