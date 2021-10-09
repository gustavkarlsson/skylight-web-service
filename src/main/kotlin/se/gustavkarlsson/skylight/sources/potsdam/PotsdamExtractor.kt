package se.gustavkarlsson.skylight.sources.potsdam

import org.intellij.lang.annotations.Language
import se.gustavkarlsson.skylight.Extractor
import se.gustavkarlsson.skylight.KpIndex
import se.gustavkarlsson.skylight.KpIndexReport
import se.gustavkarlsson.skylight.Signature
import se.gustavkarlsson.skylight.logging.logInfo

object PotsdamExtractor : Extractor<PotsdamData> {
    override fun extract(data: PotsdamData): KpIndexReport? {
        if (data.value.isBlank()) {
            logInfo { "Data is blank. Start of month?" }
            return null
        }
        val validLines = data.value.lineSequence()
            .filter { line -> line.matches(KP_LINE_REGEX) }
            .toList()
        check(validLines.isNotEmpty()) {
            "No valid lines in data:\n'${data.value}'"
        }
        val lastLine = getKpIndexLine(validLines)
        if (lastLine==null) {
            logInfo { "No line matched $KP_REGEX. Start of month? Data:\n'${data.value}'" }
            return null
        }
        val kpIndex = getLastKpIndex(lastLine)
        val signature = Signature(lastLine.hashCode())
        return KpIndexReport(kpIndex, signature)
    }

    private fun getKpIndexLine(lines: Iterable<String>): String? {
        return lines.lastOrNull { line -> line.contains(KP_REGEX) }
    }

    private fun getLastKpIndex(line: String): KpIndex {
        val lastText = line.split(Regex("\\s+")).asSequence()
            .drop(1)
            .take(8)
            .filter { cell -> cell.matches(KP_REGEX) }
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

@Language("RegExp")
private const val DATE_PATTERN = "\\d{6}"

@Language("RegExp")
private const val KP_PATTERN = "\\d[o+\\-]"

@Language("RegExp")
private const val EMPTY_SUFFIX = " *"

@Language("RegExp")
private val INCOMPLETE_SUFFIX = "( +$KP_PATTERN){0,7} *"

@Language("RegExp")
private val COMPLETE_SUFFIX = "( +$KP_PATTERN){8} +.*"

private val KP_LINE_REGEX = Regex("^${DATE_PATTERN}($EMPTY_SUFFIX|$INCOMPLETE_SUFFIX|$COMPLETE_SUFFIX)$")

private val KP_REGEX = Regex(KP_PATTERN)
