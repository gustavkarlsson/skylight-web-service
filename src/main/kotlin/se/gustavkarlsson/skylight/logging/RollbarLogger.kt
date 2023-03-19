package se.gustavkarlsson.skylight.logging

import com.rollbar.api.payload.data.Level
import com.rollbar.notifier.Rollbar

class RollbarLogger(private val rollbar: Rollbar) : Logger {

    override fun error(message: String, stackTrace: Array<StackTraceElement>, throwable: Throwable?) {
        notify(message, Level.ERROR, stackTrace, throwable)
    }

    override fun warn(message: String, stackTrace: Array<StackTraceElement>, throwable: Throwable?) {
        notify(message, Level.WARNING, stackTrace, throwable)
    }

    override fun info(message: String, stackTrace: Array<StackTraceElement>) {
        notify(message, Level.INFO, stackTrace)
    }

    override fun debug(message: String, stackTrace: Array<StackTraceElement>) {
        notify(message, Level.DEBUG, stackTrace)
    }

    private fun notify(
        message: String,
        level: Level,
        stackTrace: Array<StackTraceElement>,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            rollbar.log(throwable, message, level)
        } else {
            val stacktraceString = Exception().also {
                it.stackTrace = stackTrace
            }.stackTraceToString()
            val customData = mutableMapOf<String, Any>("stacktrace" to stacktraceString)
            rollbar.log(message, customData, level)
        }
    }
}
