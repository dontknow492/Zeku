package com.ghost.zeku

import com.ghost.zeku.logger.EnhancedColorFormatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.StreamHandler

class StdoutConsoleHandler : StreamHandler(System.out, EnhancedColorFormatter()) {
    init {
        level = Level.ALL
    }

    override fun publish(record: LogRecord) {
        super.publish(record)
        flush()
    }
}