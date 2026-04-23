package com.ghost.zeku.di


import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSettings(): Settings {
    val context: Context = org.koin.core.context.GlobalContext.get().get()
    return SharedPreferencesSettings(
        context.getSharedPreferences("settings", Context.MODE_PRIVATE),
    )
}