package se.gustavkarlsson.skylight.sources.swpc

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.skylight.KpIndex
import strikt.api.expectThat
import strikt.assertions.getValue
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.time.LocalDateTime
import java.time.ZoneOffset

class SwpcForecastExtractorSpec : Spek({
    describe("A SwpcForecastExtractor") {
        it("Gets value in last column next to geomagnetic storm") {
            val data = SwpcForecastData(validWithGeomagneticStormOnLastColumn)
            val result = SwpcForecastExtractor.extract(data)

            val instant = LocalDateTime.of(2022, 2, 20, 22, 30).toInstant(ZoneOffset.UTC)
            expectThat(result.map).getValue(instant)
                .isEqualTo(KpIndex(5f))
        }
        it("Gets value in last middle column") {
            val data = SwpcForecastData(validWithNoGeomagneticStorm)
            val result = SwpcForecastExtractor.extract(data)

            val instant = LocalDateTime.of(2022, 2, 19, 4, 30).toInstant(ZoneOffset.UTC)
            expectThat(result.map).getValue(instant)
                .isEqualTo(KpIndex(2f))
        }
        it("Gets value in middle column next to geomagnetic storm") {
            val data = SwpcForecastData(validWithGeomagneticStormOnMiddleColumn)
            val result = SwpcForecastExtractor.extract(data)

            val instant = LocalDateTime.of(2022, 2, 20, 22, 30).toInstant(ZoneOffset.UTC)
            expectThat(result.map).getValue(instant)
                .isEqualTo(KpIndex(5f))
        }
        it("Gets correct number of values when there is no geomagnetic storm") {
            val data = SwpcForecastData(validWithNoGeomagneticStorm)
            val result = SwpcForecastExtractor.extract(data)

            expectThat(result.map.keys).hasSize(24)
        }
    }
})

private val validWithGeomagneticStormOnLastColumn = """
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
""".trimIndent()

private val validWithNoGeomagneticStorm = """
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
21-00UT        1          1          5

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
""".trimIndent()

private val validWithGeomagneticStormOnMiddleColumn = """
:Product: 3-Day Forecast
:Issued: 2022 Feb 19 0030 UTC
# Prepared by the U.S. Dept. of Commerce, NOAA, Space Weather Prediction Center
#
A. NOAA Geomagnetic Activity Observation and Forecast

The greatest observed 3 hr Kp over the past 24 hours was 2 (below NOAA
Scale levels).
The greatest expected 3 hr Kp for Feb 19-Feb 21 2022 is 5 (NOAA Scale
G1).

NOAA Kp index breakdown Feb 19-Feb 21 2022

            Feb 19     Feb 20     Feb 21
00-03UT        3          2          4
03-06UT        3          1          4
06-09UT        2          1          3
09-12UT        2          1          3
12-15UT        2          3          2
15-18UT        1          4          2
18-21UT        2          4          2
21-00UT        2          5 (G1)     3

Rationale: G1 (Minor) geomagnetic storms are likely on 20 Feb due to
anticipated coronal hole influence.

B. NOAA Solar Radiation Activity Observation and Forecast

Solar radiation, as observed by NOAA GOES-16 over the past 24 hours, was
below S-scale storm level thresholds.

Solar Radiation Storm Forecast for Feb 19-Feb 21 2022

              Feb 19  Feb 20  Feb 21
S1 or greater    1%      1%      1%

Rationale: No S1 (Minor) or greater solar radiation storms are expected.
No significant active region activity favorable for radiation storm
production is forecast.

C. NOAA Radio Blackout Activity and Forecast

No radio blackouts were observed over the past 24 hours.

Radio Blackout Forecast for Feb 19-Feb 21 2022

              Feb 19        Feb 20        Feb 21
R1-R2            5%           10%           10%
R3 or greater    1%            1%            1%

Rationale: There exists the possibility for R1-R2 (Minor-Moderate) radio
blackouts over 20-21 Feb.
""".trimIndent()
