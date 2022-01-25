package se.gustavkarlsson.skylight.sources.swpc

import se.gustavkarlsson.skylight.Extractor
import se.gustavkarlsson.skylight.KpIndex
import se.gustavkarlsson.skylight.KpIndexForecastReport
import se.gustavkarlsson.skylight.Signature
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

object SwpcForecastExtractor : Extractor<SwpcForecastData, KpIndexForecastReport> {
    private val lineRegexp = Regex("\\d\\d-\\d\\dUT.*")
    private val columnRegexp = Regex("\\s+")

    @OptIn(ExperimentalStdlibApi::class)
    override fun extract(data: SwpcForecastData): KpIndexForecastReport? {
        // FIXME do some error checking
        val map = data.value.lineSequence()
            .filter { line -> line.matches(lineRegexp) }
            .map { it.split(columnRegexp) }
            .flatMap { (timeCol, day1Col, day2Col, day3Col) ->
                val startHour = timeCol.take(2).toInt()
                val endHour = timeCol.drop(3).take(2).toInt()
                val startTime = LocalTime.of(startHour, 0)
                val endTime = LocalTime.of(endHour, 0)
                val halfDuration = Duration.between(startTime, endTime).dividedBy(2)
                val time = startTime + halfDuration
                val day1 = LocalDate.now(ZoneOffset.UTC)
                val day2 = day1.plusDays(1)
                val day3 = day2.plusDays(1)

                listOf(
                    day1.atTime(time).toInstant(ZoneOffset.UTC) to KpIndex(day1Col.toFloat()),
                    day2.atTime(time).toInstant(ZoneOffset.UTC) to KpIndex(day2Col.toFloat()),
                    day3.atTime(time).toInstant(ZoneOffset.UTC) to KpIndex(day3Col.toFloat()),
                )
            }
            .toMap()
        val signature = Signature(data.hashCode()) // FIXME is this the correct thing to generate the signature?
        return KpIndexForecastReport(map, signature)
    }
}
