package com.ghost.zeku.logger

import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

class EnhancedColorFormatter : Formatter() {
    companion object {
        const val RESET = "\u001B[0m"
        const val BOLD = "\u001B[1m"
        const val RED_BOLD = "\u001B[1;31m"
        const val YELLOW_BOLD = "\u001B[1;33m"
        const val GREEN_BOLD = "\u001B[1;32m"
        const val CYAN = "\u001B[36m"
        const val BLUE = "\u001B[34m"
        const val MAGENTA = "\u001B[35m"
        const val GRAY = "\u001B[90m"
        const val WHITE = "\u001B[37m"

        // Map level to color
        val LEVEL_COLORS = mapOf(
            Level.SEVERE to RED_BOLD,
            Level.WARNING to YELLOW_BOLD,
            Level.INFO to GREEN_BOLD,
            Level.CONFIG to BLUE,
            Level.FINE to CYAN,
            Level.FINER to MAGENTA,
            Level.FINEST to GRAY
        )
    }

    override fun format(record: LogRecord): String {
        val color = LEVEL_COLORS[record.level] ?: WHITE
        return "$color[${record.level.name}] ${record.message}$RESET\n"
    }
}