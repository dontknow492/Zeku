package com.ghost.zeku.di

import com.ghost.zeku.data.local.room.getDatabaseBuilder
import org.koin.dsl.module

actual fun platformDatabaseModule() = module {
    single {
        getDatabaseBuilder()
    }
}