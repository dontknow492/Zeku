package org.ghost.zeku.core.utils

import org.ghost.zeku.core.enum.Status
import java.util.regex.Pattern

fun String.appendLineToLog(line: String = ""): String {
    val lines = this.lines().toMutableList()
    val finishingProgressLinesRegex = Pattern.compile("\\[download]\\h+(100%|[a-zA-Z])")

    if (line.isNotBlank()) {
        var newline = ""
        val newLines = line.lines().filter { !lines.contains(it) }
        lines.addAll(newLines)
        if (newLines.isNotEmpty()) {
            newLines.last().apply {
                if (this.contains("[download")) {
                    newline = "\n${this}"
                }
            }
        }


        return lines.distinct().filterNot {
            it.contains("[download") && !finishingProgressLinesRegex.matcher(it).find()
        }.joinToString("\n") + newline
    }

    return lines.filterNot {
        it.contains("[download") && !finishingProgressLinesRegex.matcher(it).find()
    }.joinToString("\n")
}


fun List<Status>.toListString(): List<String> {
    return this.map { it.toString() }
}