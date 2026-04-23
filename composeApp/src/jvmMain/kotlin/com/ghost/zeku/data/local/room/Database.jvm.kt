package com.ghost.zeku.data.local.room

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(vararg args: Any?): RoomDatabase.Builder<AppDatabase> {
    val jarPath = File(
        object {}.javaClass.protectionDomain.codeSource.location.toURI()
    )

    val installDir = jarPath.parentFile

    val dbDir = File(installDir, "database")

    val dbFile = File(dbDir, "zeku.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}