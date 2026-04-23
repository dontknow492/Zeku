package com.ghost.zeku.domain.repository

import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.model.settings.UserPreferences
import kotlinx.coroutines.flow.Flow

// UserSettings interface (Implementation would come from your DataStore/Preferences)
interface UserSettings {
    // A Flow allows the UI to react instantly when the provider changes

    val preferences: Flow<UserPreferences>

    fun setActiveProvider(type: ProviderType)
}