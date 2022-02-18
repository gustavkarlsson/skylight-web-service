package se.gustavkarlsson.skylight.sources.swpc

import se.gustavkarlsson.skylight.Extractor
import se.gustavkarlsson.skylight.KpIndex
import se.gustavkarlsson.skylight.KpIndexForecastReport
import se.gustavkarlsson.skylight.Signature
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

private val KP_LINE_REGEX = Regex("\\d\\d-\\d\\dUT.*")
private val KP_INDEX_REGEX = Regex(" +?(\\d) +?")

// FIXME test!
object SwpcForecastExtractor : Extractor<SwpcForecastData, KpIndexForecastReport> {

    @OptIn(ExperimentalStdlibApi::class)
    override fun extract(data: SwpcForecastData): KpIndexForecastReport {
        val issueDate = getIssueDate(data.value)
        val dates = getDates(issueDate, data.value)
        check(dates.isNotEmpty()) {
            "No dates found in string:\n${data.value}"
        }
        val map = data.value.lineSequence()
            .filter { line -> line.matches(KP_LINE_REGEX) }
            .flatMap { line ->
                val timeColumn = line.take(7)
                val kpColumns = KP_INDEX_REGEX.findAll(line.drop(7))
                    .map { it.groupValues[1] }
                    .toList()
                check(dates.size == kpColumns.size) {
                    "Number of dates (${dates.size}) is not equal to number of kp index columns (${kpColumns.size})"
                }
                val time = parseTime(timeColumn)

                dates.zip(kpColumns) { date, kpString ->
                    val instant = date.atTime(time).toInstant(ZoneOffset.UTC)
                    val kpIndex = KpIndex(kpString.toFloat())
                    instant to kpIndex
                }
            }
            .toMap()

        val signature = getSignature(data)
        requireNotNull(signature) {
            "No signature lines in string:\n'${data.value}'"
        }
        return KpIndexForecastReport(map, signature)
    }
}

private val ISSUE_DATE_REGEX = Regex("Issued: (\\d{4} \\w{3} \\d{1,2})")

private fun getIssueDate(text: String): LocalDate {
    val match = text.lineSequence()
        .mapNotNull { line ->
            ISSUE_DATE_REGEX.find(line)?.groupValues?.get(1)
        }
        .firstOrNull() ?: error("Could not find string matching $ISSUE_DATE_REGEX in string:\n$text")

    return LocalDate.parse(match, DateTimeFormatter.ofPattern("yyyy MMM d", Locale.US))
}

private val DAYS_REGEX = Regex(" *?(\\w{3} \\d{1,2}) *?")

private fun getDates(issueDate: LocalDate, text: String): List<LocalDate> {
    val surroundingYearStrings = listOf(
        issueDate.year - 1,
        issueDate.year,
        issueDate.year + 1
    ).map { it.toString() }
    val lines = text.lines()
    val daysLineNumber = lines.indexOfFirst { it.matches(KP_LINE_REGEX) } - 1
    check(daysLineNumber >= 0) {
        "Could not find days line in string:\n$text"
    }
    val daysLine = lines[daysLineNumber]

    return DAYS_REGEX.findAll(daysLine)
        .map { result ->
            result.groupValues[1]
        }
        .map { match ->
            surroundingYearStrings
                .map { surroundingYearString ->
                    val dateString = "$match $surroundingYearString"
                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US))
                }
                .minByOrNull { localDateCandidate ->
                    issueDate.until(localDateCandidate).toTotalMonths().absoluteValue
                }!!
        }
        .toList()
}

private fun parseTime(timeText: String): LocalTime {
    val startHour = timeText
        .take(2)
        .toInt()
    val endHour = timeText
        .drop(3)
        .take(2)
        .toInt()
        .let { hour -> if (hour < startHour) hour + 24 else hour }
    val middleSeconds = ((startHour * 3600) + (endHour * 3600)) / 2
    return LocalTime.ofSecondOfDay(middleSeconds.toLong())
}

private val SIGNATURE_REGEX = Regex(":Issued:.+")

private fun getSignature(data: SwpcForecastData): Signature? {
    return data.value.lineSequence()
        .mapNotNull { line ->
            val match = SIGNATURE_REGEX.matchEntire(line)?.value
            if (match != null) {
                val hashCode = match.hashCode()
                Signature(hashCode)
            } else null
        }
        .firstOrNull()
}

/*
:Product: 3-Day Forecast
:Issued: 2022 Feb 18 1230 UTC
# Prepared by the U.S. Dept. of Commerce, NOAA, Space Weather Prediction Center
#
A. NOAA Geomagnetic Activity Observation and Forecast

The greatest observed 3 hr Kp over the past 24 hours was 1 (below NOAA
Scale levels).
The greatest expected 3 hr Kp for Feb 18-Feb 20 2022 is 5 (NOAA Scale
G1).

NOAA Kp index breakdown Feb 18-Feb 20 2022

            Feb 18     Feb 19     Feb 20
00-03UT        0          2          2
03-06UT        1          2          1
06-09UT        1          1          1
09-12UT        1          1          1
12-15UT        1          1          3
15-18UT        1          1          4
18-21UT        2          2          4
21-00UT        1          1          5 (G1)

Rationale: G1 (Minor) geomagnetic storms are likely on 20 Feb due to CH
HSS influences.

B. NOAA Solar Radiation Activity Observation and Forecast

Solar radiation, as observed by NOAA GOES-16 over the past 24 hours, was
below S-scale storm level thresholds.

Solar Radiation Storm Forecast for Feb 18-Feb 20 2022

              Feb 18  Feb 19  Feb 20
S1 or greater    1%      1%      1%

Rationale: No S1 (Minor) or greater solar radiation storms are expected.
No significant active region activity favorable for radiation storm
production is forecast.

C. NOAA Radio Blackout Activity and Forecast

No radio blackouts were observed over the past 24 hours.

Radio Blackout Forecast for Feb 18-Feb 20 2022

              Feb 18        Feb 19        Feb 20
R1-R2            5%            5%           10%
R3 or greater    1%            1%            1%

Rationale: There is a slight chance for R1-R2 (Minor-Moderate) radio
blackouts on 20 Feb due to the return of old Region 2936 (N17, L=132).
*/
