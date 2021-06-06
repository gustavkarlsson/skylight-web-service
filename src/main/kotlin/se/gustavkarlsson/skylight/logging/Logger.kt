package se.gustavkarlsson.skylight.logging

interface Logger {
    fun error(message: () -> String, stackTrace: Array<StackTraceElement>, throwable: Throwable?)
    fun warn(message: () -> String, stackTrace: Array<StackTraceElement>, throwable: Throwable?)
    fun info(message: () -> String, stackTrace: Array<StackTraceElement>)
    fun debug(message: () -> String, stackTrace: Array<StackTraceElement>)
}
