package se.gustavkarlsson.skylight.sources.potsdam

import se.gustavkarlsson.skylight.Extractor
import se.gustavkarlsson.skylight.KpIndex
import se.gustavkarlsson.skylight.KpIndexReport
import se.gustavkarlsson.skylight.Signature

object PotsdamExtractor : Extractor<PotsdamData> {
    override fun extract(data: PotsdamData): KpIndexReport {
        val lastLine = getLastLine(data)
        val kpIndex = getLastKpIndex(lastLine)
        val signature = Signature(lastLine.hashCode())
        return KpIndexReport(kpIndex, signature)
    }

    private fun getLastLine(data: PotsdamData): String {
        return data.value.lineSequence()
            .filter { line -> line.isNotBlank() }
            .last()
    }

    private fun getLastKpIndex(line: String): KpIndex {
        val lastText = line.split(Regex("\\s+")).asSequence()
            .drop(1)
            .take(8)
            .filter { string -> string.matches(Regex("[0-9]([o+\\-])")) }
            .lastOrNull() ?: throw IllegalArgumentException("Could not find Kp index in line: '$line'")
        return lastText.parseKpIndex()
    }

    private fun String.parseKpIndex(): KpIndex {
        val whole = parseWhole()
        val extra = parseExtra()
        return KpIndex(whole + extra)
    }

    private fun String.parseWhole(): Float {
        val firstChar = substring(0, 1)
        return firstChar.toFloat()
    }

    private fun String.parseExtra(): Float {
        return when (substring(1)) {
            "-" -> -0.33f
            "+" -> 0.33f
            "o" -> 0f
            else -> throw IllegalArgumentException("Invalid Kp index format: text")
        }
    }

}
