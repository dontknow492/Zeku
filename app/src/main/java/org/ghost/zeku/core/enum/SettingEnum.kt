package org.ghost.zeku.core.enum

import androidx.annotation.StringRes

/**
 * An interface for enums used in settings.
 * Guarantees that the enum has a stable [value] for storage
 * and a user-friendly [label] for display.
 */
interface SettingEnum {
    val value: String // The stable ID for storage
    val label: String // The name for the UI
    @get: StringRes
    val descriptionResId: Int?
}