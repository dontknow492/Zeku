package com.ghost.zeku.data.local.room

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ghost.zeku.data.local.room.dao.*
import com.ghost.zeku.data.local.room.entities.*
import com.ghost.zeku.data.local.room.view.MediaLibraryView
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        UserEntity::class,
        MediaEntity::class,
        MediaSearchEntity::class,

        EpisodeEntity::class,
        ChapterEntity::class,

        MediaRemoteKeys::class,
        MediaDetailsEntity::class,

        EpisodeRemoteKeys::class,
        ChapterRemoteKeys::class,
        LibraryEntity::class,
        LibraryCategoryEntity::class,
        TrackEntryEntity::class
    ],
    views = [
        MediaLibraryView::class  // Register the view here
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun chapterDao(): ChapterDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun userDao(): UserDao
    abstract fun libraryDao(): LibraryDao
    abstract fun libraryCategoryDao(): LibraryCategoryDao
    abstract fun trackEntryDao(): TrackEntryDao
}


// This is required for Room KMP to generate the implementation
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Expect function to be implemented in androidMain and desktopMain.
 * It provides the platform-specific builder.
 */
expect fun getDatabaseBuilder(vararg args: Any?): RoomDatabase.Builder<AppDatabase>

/**
 * Helper to get an instance of the database.
 * You should ideally call this through Dependency Injection (like Koin).
 */
fun createDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        // Add the driver here
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}