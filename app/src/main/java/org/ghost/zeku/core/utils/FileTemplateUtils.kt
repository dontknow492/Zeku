package org.ghost.zeku.core.utils

import android.content.Context
import org.ghost.zeku.R

object FileTemplateUtils {
    val allTemplatePlaceholders: Map<Int, String> = mapOf(
        // Core Identifiers
        R.string.video_id to "%(id)s",
        R.string.title to "%(title)s",
        R.string.full_title to "%(fulltitle)s",
        R.string.alternate_title to "%(alt_title)s",
        R.string.file_extension to "%(ext)s",

        // Uploader & Channel
        R.string.uploader_name to "%(uploader)s",
        R.string.uploader_id to "%(uploader_id)s",
        R.string.uploader_url to "%(uploader_url)s",
        R.string.channel_name to "%(channel)s",
        R.string.channel_id to "%(channel_id)s",
        R.string.channel_url to "%(channel_url)s",

        // Date & Time
        R.string.upload_date to "%(upload_date)s",
        R.string.release_date to "%(release_date)s",
        R.string.publication_timestamp to "%(timestamp)s",
        R.string.duration_seconds to "%(duration)s",
        R.string.duration_hms to "%(duration_string)s",

        // Format & Quality
        R.string.format_description to "%(format)s",
        R.string.format_id to "%(format_id)s",
        R.string.resolution_wh to "%(resolution)s",
        R.string.video_width to "%(width)s",
        R.string.video_height to "%(height)s",
        R.string.frames_per_second to "%(fps)s",
        R.string.video_bitrate to "%(vbr)s",
        R.string.audio_bitrate to "%(abr)s",
        R.string.video_codec to "%(vcodec)s",
        R.string.audio_codec to "%(acodec)s",

        // Playlist & Series
        R.string.playlist_title to "%(playlist)s",
        R.string.playlist_id to "%(playlist_id)s",
        R.string.playlist_index to "%(playlist_index)s",
        R.string.playlist_entry_count to "%(n_entries)s",
        R.string.series_title to "%(series)s",
        R.string.season_name to "%(season)s",
        R.string.season_number to "%(season_number)s",
        R.string.episode_title to "%(episode)s",
        R.string.episode_number to "%(episode_number)s",

        // Music Metadata
        R.string.artist to "%(artist)s",
        R.string.album to "%(album)s",
        R.string.album_artist to "%(album_artist)s",
        R.string.track_title to "%(track)s",
        R.string.track_number to "%(track_number)s",
        R.string.disc_number to "%(disc_number)s",
        R.string.release_year to "%(release_year)s",
        R.string.genre to "%(genre)s",

        // Chapter Metadata
        R.string.chapter_title to "%(chapter)s",
        R.string.chapter_number to "%(chapter_number)s",

        // Engagement Metrics
        R.string.view_count to "%(view_count)s",
        R.string.like_count to "%(like_count)s",
        R.string.dislike_count to "%(dislike_count)s",
        R.string.comment_count to "%(comment_count)s",

        // System & File
        R.string.current_timestamp_epoch to "%(epoch)s",
        R.string.file_size_bytes to "%(filesize)s",
        R.string.approx_file_size_bytes to "%(filesize_approx)s",
        R.string.original_filename to "%(filename)s",

        // Miscellaneous
        R.string.webpage_url to "%(webpage_url)s",
        R.string.video_description to "%(description)s",
        R.string.age_limit to "%(age_limit)s"
    )


    /**
     * Checks if a given string is a valid yt-dlp filename template.
     *
     * This function works by:
     * 1. Finding all potential placeholders that match the "%(key)s" syntax.
     * 2. For each placeholder, it extracts the key(s) inside.
     * 3. It handles fallbacks (e.g., "%(artist, channel)s") by checking each key individually.
     * 4. It checks if every extracted key is present in a master set of known valid keys.
     *
     * @param template The filename template string to validate.
     * @return `true` if all found placeholders use valid keys, `false` otherwise.
     */
    fun isValidFileTemplate(template: String): Boolean {
        // A comprehensive set of all known valid yt-dlp placeholder keys.
        val validKeys = setOf(
            "id", "title", "fulltitle", "alt_title", "ext", "uploader", "uploader_id",
            "uploader_url", "channel", "channel_id", "channel_url", "upload_date",
            "release_date", "timestamp", "duration", "duration_string", "format",
            "format_id", "resolution", "width", "height", "fps", "vbr", "abr",
            "vcodec", "acodec", "playlist", "playlist_id", "playlist_index",
            "n_entries", "series", "season", "season_number", "episode",
            "episode_number", "artist", "album", "album_artist", "track",
            "track_number", "disc_number", "release_year", "genre", "chapter",
            "chapter_number", "view_count", "like_count", "dislike_count",
            "comment_count", "epoch", "filesize", "filesize_approx", "filename",
            "webpage_url", "description", "age_limit"
        )

        // Regex to find all occurrences of the "%(content)X" pattern, where X is a letter.
        // It captures the "content" part inside the parentheses.
        val placeholderRegex = Regex("""%\(([^)]+)\)([a-zA-Z])""")

        val matches = placeholderRegex.findAll(template)

        // Iterate over every placeholder found in the template string.
        for (match in matches) {
            // The captured content, e.g., "title" or "artist, channel" or "playlist_index)02".
            val content = match.groupValues[1]

            // Handle potential formatting suffixes like in "%(playlist_index)02d".
            // We only care about the part before the optional formatting parenthesis.
            val mainContent = content.split(')').first()

            // Handle fallbacks like in "%(artist, channel)s" by splitting by the comma.
            val keysToCheck = mainContent.split(',').map { it.trim() }

            // Check if every single key in this placeholder is in our valid set.
            for (key in keysToCheck) {
                if (key !in validKeys) {
                    // If we find even one invalid key, the whole template is considered invalid.
                    // You could also log the invalid key here for debugging: println("Invalid key found: $key")
                    return false
                }
            }
        }

        // If we've checked all placeholders and found no invalid keys, the template is valid.
        return true
    }


    fun getAvailableTemplates(): Map<Int, String> {
        return allTemplatePlaceholders
    }

    fun getAvailableTemplateResId(): List<Int> {
        return allTemplatePlaceholders.keys.toList()
    }

    fun getLabel(context: Context, placeholder: String): String? {
        val entry = allTemplatePlaceholders.entries.find { it.value == placeholder } ?: return null
        return context.getString(entry.key)
    }

    /** Get placeholder by label */
    fun getPlaceholder(context: Context, label: String): String? {
        val entry = allTemplatePlaceholders.entries.find {
            context.getString(it.key).equals(label, ignoreCase = true)
        }
        return entry?.value
    }

    /** Returns all (translatedLabel to placeholder) pairs */
    fun getLocalizedTemplates(context: Context): Map<String, String> {
        return allTemplatePlaceholders.mapKeys { (resId, _) -> context.getString(resId) }
    }

}