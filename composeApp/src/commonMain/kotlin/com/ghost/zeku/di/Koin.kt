package com.ghost.zeku.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            networkModule,
            providerModule,
            repoModule,
            settingsModule,
            platformDatabaseModule(),
            databaseModule,
            platformSettingsModule(),
            settingsModule,
            authModule
        )
    }
}

// A secondary initializer for iOS/Desktop if you need to call it without parameters
fun initKoin() = initKoin {}