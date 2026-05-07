package com.ghost.zeku.domain.repository

import com.ghost.zeku.domain.model.settings.UserPreferences
import kotlinx.coroutines.flow.StateFlow

interface UserSettings {
    val preferences: StateFlow<UserPreferences>

    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences): Result<Unit>


    /**
     * Exports the current UserPreferences as a serialized JSON string.
     * The UI can save this string to a .json file on the device.
     */
    fun exportSettingsJson(): String

    /**
     * Imports a JSON string, validates it, and overwrites the current preferences.
     * @return true if successful, false if the JSON is corrupted/invalid.
     */
    suspend fun importSettingsJson(jsonString: String): Boolean
}