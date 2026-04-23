package com.ghost.zeku.di

import com.ghost.zeku.data.settings.UserSettingsImpl
import com.ghost.zeku.domain.repository.UserSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module


expect fun createSettings(): Settings


val settingsModule = module {
    // Provides the platform-specific implementation of Settings
    single<Settings> { createSettings() }

    single<UserSettings> {
        UserSettingsImpl(get())
    }
}