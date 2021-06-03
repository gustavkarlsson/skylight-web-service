package se.gustavkarlsson.skylight

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun logError(t: Throwable? = null, message: () -> String) {
    val logger = getLogger()
    if (!logger.isErrorEnabled) return
    logger.error(message(), t)
}

fun logWarn(t: Throwable? = null, message: () -> String) {
    val logger = getLogger()
    if (!logger.isWarnEnabled) return
    logger.warn(message(), t)
}

fun logInfo(message: () -> String) {
    val logger = getLogger()
    if (!logger.isInfoEnabled) return
    logger.info(message())
}

fun logDebug(message: () -> String) {
    val logger = getLogger()
    if (!logger.isDebugEnabled) return
    logger.debug(message())
}

private fun getLogger(): Logger {
    val stackTrace = Thread.currentThread().stackTrace
    val name = getLoggerName(stackTrace)
    return LoggerFactory.getLogger(name)
}

private fun getLoggerName(stackTrace: Array<StackTraceElement>): String {
    stackDepth?.let { depth ->
        return stackTrace[depth].className
    }
    val classNameToIgnore = stackTrace[1].className
    val depth = stackTrace
        .map { it.className }
        .withIndex()
        .indexOfFirst { (index, className) ->
            index > 0 && className!=classNameToIgnore
        }
    stackDepth = depth
    return stackTrace[depth].className
}

private var stackDepth: Int? = null
