package org.ghost.zeku.database

import androidx.room.TypeConverter
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.ghost.zeku.core.enum.MediaType
import org.ghost.zeku.core.enum.Status
import org.ghost.zeku.database.models.AudioPreferences
import org.ghost.zeku.database.models.Format
import org.ghost.zeku.database.models.VideoPreferences
import timber.log.Timber

/**
 * Type Converters for the Room Database.
 * These converters handle serialization and deserialization of complex
 * types (Data Classes, Lists, Enums) that Room cannot natively store.
 */
class Converters {

    // --- JSON Utility Instance (Kotlinx.serialization) ---
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    // --- TypeConverter for the Format Data Class ---

    @TypeConverter
    fun fromFormat(format: Format): String {
        return json.encodeToString(format)
    }

    @TypeConverter
    fun toFormat(formatJson: String): Format {
        // Safe decoding, returning a default Format on failure (if format is non-nullable)
        return try {
            json.decodeFromString(formatJson)
        } catch (e: Exception) {
            // Handle error case, e.g., logging it and returning an empty object
            Timber.e("Error deserializing Format: ${e.message}")
            Format()
        }
    }

    @TypeConverter
    fun fromMutableListFormat(allFormat: MutableList<Format>): String {
        return json.encodeToString(allFormat)
    }

    @TypeConverter
    fun toMutableListFormat(allFormatJson: String): MutableList<Format> {
        return try {
            json.decodeFromString(allFormatJson)
        } catch (e: SerializationException) {
            Timber.e("Error deserializing Mutable list Format: ${e.message}")
            mutableListOf()
        }
    }

    // --- TypeConverter for List<String> (Used in downloadPath, urlRegex, etc.) ---

    @TypeConverter
    fun fromListOfStrings(list: List<String>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun toListOfStrings(jsonString: String): List<String> {
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            Timber.e("Error deserializing List<String>: ${e.message}")
            emptyList()
        }
    }

    // --- TypeConverter for MediaType Enum (Assuming MediaType is a simple Enum) ---

    @TypeConverter
    fun fromMediaType(type: MediaType): String {
        return type.name
    }

    @TypeConverter
    fun toMediaType(name: String): MediaType {
        return try {
            MediaType.valueOf(name)
        } catch (e: IllegalArgumentException) {
            // Defaulting to a safe, generic type if the saved name is unknown
            MediaType.VIDEO
        }
    }

    @TypeConverter
    fun fromStatus(status: Status): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(name: String): Status {
        return try {
            Status.valueOf(name)
        } catch (e: IllegalArgumentException) {
            // Defaulting to a safe, generic type if the saved name is unknown
            Status.Error
        }
    }

    @TypeConverter
    fun fromMutableListOfStrings(list: MutableList<String>): String {
        // Serializes a MutableList just like a regular list
        return json.encodeToString(list)
    }

    @TypeConverter
    fun toMutableListOfStrings(jsonString: String): MutableList<String> {
        // Deserializes explicitly to a MutableList
        return try {
            json.decodeFromString<MutableList<String>>(jsonString)
        } catch (e: Exception) {
            Timber.e("Error deserializing MutableList<String>: ${e.message}")
            mutableListOf()
        }
    }


    @TypeConverter
    fun fromAudioPreferences(preferences: AudioPreferences): String {
        return json.encodeToString(preferences)
    }

    @TypeConverter
    fun toAudioPreferences(jsonString: String): AudioPreferences {
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            Timber.e("Error deserializing AudioPreferences: ${e.message}")
            AudioPreferences()
        }
    }

    // --- TypeConverter for VideoPreferences Data Class ---

    @TypeConverter
    fun fromVideoPreferences(preferences: VideoPreferences): String {
        return json.encodeToString(preferences)
    }

    @TypeConverter
    fun toVideoPreferences(jsonString: String): VideoPreferences {
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            Timber.e("Error deserializing VideoPreferences: ${e.message}")
            VideoPreferences()
        }
    }

}