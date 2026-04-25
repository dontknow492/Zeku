package com.ghost.zeku.logger

import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

class DistinctColorFormatter : Formatter() {
    // ANSI escape codes for better visibility
    companion object {
        private const val RESET = "\u001B[0m"
        private const val BOLD = "\u001B[1m"

        // Distinct colors for each log level
        private val COLOR_MAP = mapOf(
            Level.SEVERE to "\u001B[1;31m", // bold red
            Level.WARNING to "\u001B[1;33m", // bold yellow
            Level.INFO to "\u001B[1;32m", // bold green
            Level.CONFIG to "\u001B[34m",   // blue
            Level.FINE to "\u001B[36m",   // cyan
            Level.FINER to "\u001B[35m",   // magenta
            Level.FINEST to "\u001B[90m"    // dark gray
        )
    }

    override fun format(record: LogRecord): String {
        val color = COLOR_MAP[record.level] ?: "\u001B[37m" // white fallback
        // Format: colorized [LEVEL] message, then reset
        return "$color[${record.level.name}] ${record.message}$RESET\n"
    }
}