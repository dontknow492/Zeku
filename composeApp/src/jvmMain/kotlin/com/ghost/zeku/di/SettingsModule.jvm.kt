package com.ghost.zeku.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createSettings(): Settings {
    return PreferencesSettings(Preferences.userRoot().node("com/ghost/zeku/settings"))
}

