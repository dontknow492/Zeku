package org.ghost.zeku.core.utils

/**
 * Returns a copy of this string with the first character capitalized and the rest lowercased.
 * If the string is empty, returns the original string.
 */
fun String.titlecase(): String {
    return if (this.isNotEmpty()) {
        this.substring(0, 1).uppercase() + this.substring(1).lowercase()
    } else {
        this
    }
}