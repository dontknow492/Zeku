package org.ghost.zeku.core.enum

enum class PreventDuplicateDownload(val value: Int, val description: String) {
    NONE(0, "Disabled"),
    URL(1, "Based on media url"),
    TYPE_AND_URL(2, "Based on media type and url"),
    CONFIGURATION(3, "Based on media download configuration");

    companion object {
        fun fromValue(value: Int): PreventDuplicateDownload {
            return entries.firstOrNull { it.value == value } ?: NONE
        }
    }
}