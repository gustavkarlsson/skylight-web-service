package se.gustavkarlsson.skylight.sources.potsdam

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.skylight.KpIndex
import se.gustavkarlsson.skylight.KpIndexReport
import se.gustavkarlsson.skylight.Signature
import strikt.api.DescribeableBuilder
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

object PotsdamExtractorSpec : Spek({
    describe("A PotsdamExtractor") {
        val subject by memoized { PotsdamExtractor }

        it("extracts kp index report from report 1") {
            val data = PotsdamData(report1)
            val result = subject.extract(data)

            expectThat(result).isRoughlyEqualTo(KpIndexReport(KpIndex(1.66f), Signature(171991982)))
        }

        it("extracts kp index report from report 2") {
            val data = PotsdamData(report2)
            val result = subject.extract(data)

            expectThat(result).isRoughlyEqualTo(KpIndexReport(KpIndex(1f), Signature(247203774)))
        }

        it("extracts kp index report from report 3") {
            val data = PotsdamData(report3)
            val result = subject.extract(data)

            expectThat(result).isRoughlyEqualTo(KpIndexReport(KpIndex(0.66f), Signature(-65558901)))
        }

        it("throws exception on corrupt report") {
            val data = PotsdamData(corruptReport1)

            expectThrows<Exception> {
                subject.extract(data)
            }
        }
    }
})

private fun DescribeableBuilder<KpIndexReport>.isRoughlyEqualTo(kpIndexReport: KpIndexReport) {
    describedAs("result").run {
        get { kpIndex.value }.describedAs("kp index")
            .isEqualTo(kpIndexReport.kpIndex.value, 0.01)
        get { signature.value }.describedAs("signature")
            .isEqualTo(kpIndexReport.signature.value)
    }
}

private val report1 = """
    180724  4- 3o 3+ 3-  2- 3- 2+ 3+   23-     14 0.8
    180725  3- 3+ 2-
""".trimIndent()

private val report2 = """
    180722  2- 1o 1+ 1+  1o 1+ 1- 1-    9o      4 0.2
    180723  0+ 1- 1- 1-  0+ 0+ 1o 1o    5o      3 0.1
""".trimIndent()

private val report3 = """
    180722  2- 1o 1+ 1+  1o 1+ 1- 1-    9o      4 0.2
    180723
""".trimIndent()

private val corruptReport1 = """
    18070 feawfwewa  wf
""".trimIndent()
