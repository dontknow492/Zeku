package org.ghost.zeku.core.enum

import androidx.annotation.StringRes

/**
 * Represents the different categories of segments defined by SponsorBlock.
 *
 * @property value The string type used by the SponsorBlock API and yt-dlp.
 * @property displayName A user-friendly name for UI elements.
 */
enum class SponsorBlockCategory(
    override val value: String,
    override val label: String,
    @param: StringRes override val descriptionResId: Int? = null,
) : SettingEnum {
    SPONSOR("sponsor", "Sponsor"),
    INTRO("intro", "Intro"),
    OUTRO("outro", "Outro"),
    INTERACTION("interaction", "Interaction Reminder"),
    SELF_PROMO("selfpromo", "Self Promotion"),
    MUSIC_OFFTOPIC("music_offtopic", "Non-Music Segment"),
    PREVIEW_RECAP("preview", "Preview/Recap"),
    FILLER("filler", "Filler Tangent");

    companion object {
        /**
         * A set of all category values for convenience.
         */
        val ALL_VALUES: Set<String> = values().mapTo(HashSet()) { it.value }

        /**
         * Finds a SponsorBlockCategory by its string type, returning null if not found.
         * This is safer than `valueOf()` which throws an exception.
         */
        fun fromValue(value: String?): SponsorBlockCategory? {
            return values().find { it.value.equals(value, ignoreCase = true) }
        }
    }
}