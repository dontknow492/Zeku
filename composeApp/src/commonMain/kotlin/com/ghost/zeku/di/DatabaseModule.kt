package com.ghost.zeku.di

import androidx.room.RoomDatabase
import com.ghost.zeku.data.local.room.AppDatabase
import com.ghost.zeku.data.local.room.createDatabase
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Common module for Database injection.
 * Each platform will provide its own implementation of getDatabaseModule().
 */
expect fun platformDatabaseModule(): Module

val databaseModule = module {
    // Provide the Database instance
    // Note: This 'get()' will be fulfilled by the platform-specific builder
    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        createDatabase(builder)
    }

    // Provide DAOs for easy injection into Repositories
    single { get<AppDatabase>().animeDao() }
    single { get<AppDatabase>().mangaDao() }
    single { get<AppDatabase>().remoteKeysDao() }

}