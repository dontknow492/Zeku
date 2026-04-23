package com.ghost.zeku.di

import com.ghost.zeku.data.local.room.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformDatabaseModule() = module {
    single {
        // Passes the androidContext provided during Koin initialization
        getDatabaseBuilder(androidContext())
    }
}