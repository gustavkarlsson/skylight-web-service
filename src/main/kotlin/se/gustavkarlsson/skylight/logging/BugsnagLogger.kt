package se.gustavkarlsson.skylight.logging

import com.bugsnag.Bugsnag
import com.bugsnag.Severity

class BugsnagLogger(private val bugsnag: Bugsnag) : Logger {
    override fun error(message: () -> String, stackTrace: Array<StackTraceElement>, throwable: Throwable?) {
        notify(message(), throwable, stackTrace, Severity.ERROR)
    }

    override fun warn(message: () -> String, stackTrace: Array<StackTraceElement>, throwable: Throwable?) {
        notify(message(), throwable, stackTrace, Severity.WARNING)
    }

    override fun info(message: () -> String, stackTrace: Array<StackTraceElement>) = Unit

    override fun debug(message: () -> String, stackTrace: Array<StackTraceElement>) = Unit

    private fun notify(
        message: String,
        throwable: Throwable?,
        stackTrace: Array<StackTraceElement>,
        severity: Severity,
    ) {
        val exception = throwable ?: MessageException(message, stackTrace)
        bugsnag.notify(exception, severity)
    }
}

private class MessageException(
    message: String,
    overrideStackTrace: Array<StackTraceElement>,
) : RuntimeException(message) {
    init {
        stackTrace = overrideStackTrace
    }
}
