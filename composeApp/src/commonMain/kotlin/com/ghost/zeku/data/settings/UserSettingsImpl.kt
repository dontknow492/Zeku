package com.ghost.zeku.data.settings

//import com.russhwolf.settings.Settings
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.settings.UserPreferences
import com.ghost.zeku.domain.repository.UserSettings
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserSettingsImpl(
    private val settings: Settings
) : UserSettings {

    companion object {
        private const val KEY_ACTIVE_PROVIDER = "active_provider"
    }

    // Initialize the flow by loading the saved state from disk once
    private val _preferences = MutableStateFlow(loadPreferences())
    override val preferences = _preferences.asStateFlow()

    /**
     * Loads the initial state from Multiplatform Settings.
     * We use this only once during class initialization.
     */
    private fun loadPreferences(): UserPreferences {
        val providerName = settings.getString(KEY_ACTIVE_PROVIDER, ProviderType.ANILIST.name)

        val provider = try {
            ProviderType.valueOf(providerName)
        } catch (e: Exception) {
            Napier.w("Saved provider '$providerName' is invalid, falling back to ANILIST", e)
            ProviderType.ANILIST
        }

        return UserPreferences(activeProvider = provider)
    }

    /**
     * Updates both the persistent storage and the reactive flow.
     */
    override fun setActiveProvider(type: ProviderType) {
        // 1. Persist to disk
        settings.putString(KEY_ACTIVE_PROVIDER, type.name)

        // 2. Notify all observers (like MediaRepository and UI)
        _preferences.update { it.copy(activeProvider = type) }
    }
}