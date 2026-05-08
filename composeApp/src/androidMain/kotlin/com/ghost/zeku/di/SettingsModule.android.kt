package com.ghost.zeku.di

import com.ghost.zeku.data.settings.SettingsFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific module to provide the Factory with Context.
 */
actual fun platformSettingsModule() = module {
    single { SettingsFactory(androidContext()) }
}