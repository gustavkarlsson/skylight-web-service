package se.gustavkarlsson.skylight

fun interface Extractor<in In, out Out : Any> {
    fun extract(data: In): Out?
}
