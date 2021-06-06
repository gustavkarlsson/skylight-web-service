package se.gustavkarlsson.skylight

fun interface Extractor<In> {
    fun extract(data: In): KpIndexReport
}
