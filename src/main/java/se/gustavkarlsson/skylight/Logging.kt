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
    val frame = stackTrace[3]
    val name = frame.className
    return LoggerFactory.getLogger(name)
}
