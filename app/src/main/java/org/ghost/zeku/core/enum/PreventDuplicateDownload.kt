package org.ghost.zeku.core.enum

import androidx.annotation.StringRes
import org.ghost.zeku.R

enum class PreventDuplicateDownload(
    override val value: String,      // The stable ID is now a readable string.
    override val label: String,      // The UI-friendly label.
    @param: StringRes override val descriptionResId: Int? // Now holds the R.string ID.
) : SettingEnum {
    NONE(
        value = "none",
        label = "None",
        descriptionResId = R.string.prevent_duplicate_none_desc
    ),
    URL(
        value = "url",
        label = "Url",
        descriptionResId = R.string.prevent_duplicate_url_desc
    ),
    TYPE_AND_URL(
        value = "type_and_url",
        label = "Type and Url",
        descriptionResId = R.string.prevent_duplicate_type_and_url_desc
    ),
    CONFIGURATION(
        value = "configuration",
        label = "Configuration",
        descriptionResId = R.string.prevent_duplicate_configuration_desc
    );

    companion object {
        /**
         * Safely finds an enum constant from a stored string type.
         * Defaults to NONE if the type is null or not found.
         */
        fun fromValue(value: String?): PreventDuplicateDownload {
            return entries.firstOrNull { it.value == value } ?: NONE
        }
    }
}