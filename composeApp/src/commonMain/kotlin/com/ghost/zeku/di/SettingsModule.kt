package com.ghost.zeku.di

import com.ghost.zeku.data.settings.SettingsFactory
import com.ghost.zeku.data.settings.UserSettingsImpl
import com.ghost.zeku.domain.repository.UserSettings
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


expect fun platformSettingsModule(): Module

val settingsModule = module {


    // 1. Create the named Settings instances using our new Factory
    single(named("standardSettings")) { get<SettingsFactory>().createStandardSettings() }
    single(named("secureSettings")) { get<SettingsFactory>().createSecureSettings() }


    single<UserSettings> { UserSettingsImpl(settings = get(named("standardSettings"))) }
}