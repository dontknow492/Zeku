package org.ghost.zeku.core.enum

enum class ThemeMode {
    LIGHT, DARK, SYSTEM;

    companion object {
        fun fromString(value: String): ThemeMode {
            return when (value) {
                "light" -> LIGHT
                "dark" -> DARK
                else -> SYSTEM
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            LIGHT -> "light"
            DARK -> "dark"
            SYSTEM -> "system"
        }
    }
}