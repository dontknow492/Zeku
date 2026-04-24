package com.ghost.zeku.di

import com.ghost.zeku.data.settings.SettingsFactory
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Desktop-specific module.
 */
actual fun platformSettingsModule() = module {
    single { SettingsFactory() }
}