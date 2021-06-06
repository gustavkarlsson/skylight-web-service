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
    val stackTrace = Thread.currentThread().stackTrace
    val thisFileName = stackTrace[1].fileName
    val depth = stackTrace
        .map { it.fileName }
        .withIndex()
        .indexOfFirst { (index, fileName) ->
            index > 0 && fileName!=thisFileName
        }
    return stackTrace.drop(depth)
}

private fun Array<StackTraceElement>.drop(depth: Int) = copyOfRange(depth, size)
