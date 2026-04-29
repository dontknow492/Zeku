package com.ghost.zeku.data.settings

import com.ghost.zeku.domain.model.settings.UserPreferences
import com.ghost.zeku.domain.repository.UserSettings
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

class UserSettingsImpl(
    private val settings: Settings
) : UserSettings {

    companion object {
        private const val KEY_PREFS_JSON = "user_preferences_json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val _preferences = MutableStateFlow(loadPreferences())
    override val preferences = _preferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val jsonString = settings.getString(KEY_PREFS_JSON, "")

        if (jsonString.isBlank()) {
            return UserPreferences()
        }

        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            Napier.e(e) { "Failed to parse UserPreferences JSON. Corrupted data? Falling back to defaults." }
            UserPreferences()
        }
    }

    override fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        Napier.d { "Updating preferences..." }
        _preferences.update { currentPrefs ->
            val newPrefs = transform(currentPrefs)
            saveToDisk(newPrefs)
            Napier.d { "Preferences updated." }
            newPrefs
        }
    }

    private fun saveToDisk(prefs: UserPreferences) {
        try {
            val jsonString = json.encodeToString(prefs)
            settings.putString(KEY_PREFS_JSON, jsonString)
        } catch (e: Exception) {
            Napier.e(e) { "Failed to save UserPreferences to disk." }
        }
    }

    // ========================================================================
    // BACKUP & RESTORE
    // ========================================================================

    override fun exportSettingsJson(): String {
        return try {
            // Encode the current state flow value to a pretty/standard JSON string
            json.encodeToString(_preferences.value)
        } catch (e: Exception) {
            Napier.e(e) { "Failed to export settings to JSON string." }
            ""
        }
    }

    override fun importSettingsJson(jsonString: String): Boolean {
        if (jsonString.isBlank()) return false

        return try {
            // 1. Attempt to decode the string. If it's bad JSON, it throws an exception here.
            val importedPrefs = json.decodeFromString<UserPreferences>(jsonString)

            // 2. If decoding succeeds, apply it using our unified updater!
            updatePreferences { importedPrefs }

            Napier.i { "Successfully restored settings from backup." }
            true
        } catch (e: Exception) {
            Napier.e(e) { "Failed to restore settings. The provided JSON is invalid or corrupted." }
            false
        }
    }
}









