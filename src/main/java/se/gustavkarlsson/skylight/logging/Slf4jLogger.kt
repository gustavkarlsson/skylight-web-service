package se.gustavkarlsson.skylight.logging

import org.slf4j.LoggerFactory
import org.slf4j.Logger as Slf4jLogger

object Slf4jLogger : Logger {
    override fun error(message: () -> String, stackTrace: Array<StackTraceElement>, throwable: Throwable?) {
        val logger = getLogger(stackTrace)
        if (!logger.isErrorEnabled) return
        logger.error(message(), throwable)
    }

    override fun warn(message: () -> String, stackTrace: Array<StackTraceElement>, throwable: Throwable?) {
        val logger = getLogger(stackTrace)
        if (!logger.isWarnEnabled) return
        logger.warn(message(), throwable)
    }

    override fun info(message: () -> String, stackTrace: Array<StackTraceElement>) {
        val logger = getLogger(stackTrace)
        if (!logger.isInfoEnabled) return
        logger.info(message())
    }

    override fun debug(message: () -> String, stackTrace: Array<StackTraceElement>) {
        val logger = getLogger(stackTrace)
        if (!logger.isDebugEnabled) return
        logger.debug(message())
    }
}

private fun getLogger(stackTrace: Array<StackTraceElement>): Slf4jLogger {
    val name = stackTrace.first().className
    return LoggerFactory.getLogger(name)
}
