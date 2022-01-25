package se.gustavkarlsson.skylight

fun interface Extractor<In, Out : Any> {
    fun extract(data: In): Out?
}
