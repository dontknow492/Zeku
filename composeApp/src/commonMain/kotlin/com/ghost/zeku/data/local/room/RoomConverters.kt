package com.ghost.zeku.data.local.room


import androidx.room.TypeConverter
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.media.MediaDetails
import kotlinx.serialization.json.Json

/**
 * Room cannot store complex objects (like Lists or custom data classes) in a single column.
 * These converters use Kotlinx Serialization to turn your objects into JSON strings for storage,
 * and back into objects when reading from the database.
 * * Note: Make sure MediaTitle, MediaDate, and TrackEntry are annotated with @Serializable!
 */
class RoomConverters {
    private val json = Json { ignoreUnknownKeys = true }

    // --- List<String> (Genres) ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- MediaTitle ---
    @TypeConverter
    fun fromMediaTitle(value: MediaTitle?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toMediaTitle(value: String?): MediaTitle? {
        return value?.let { json.decodeFromString(it) }
    }

    // --- MediaDate ---
    @TypeConverter
    fun fromMediaDate(value: MediaDate?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toMediaDate(value: String?): MediaDate? {
        return value?.let { json.decodeFromString(it) }
    }

    // --- TrackEntry ---
    @TypeConverter
    fun fromTrackEntry(value: TrackEntry?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toTrackEntry(value: String?): TrackEntry? {
        return value?.let { json.decodeFromString(it) }
    }


    // --- Details ---
    @TypeConverter
    fun fromMediaDetails(value: MediaDetails?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toMediaDetails(value: String?): MediaDetails? {
        return value?.let { json.decodeFromString(it) }
    }

}