package com.ghost.zeku.data.local.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase


actual fun getDatabaseBuilder(vararg args: Any?): RoomDatabase.Builder<AppDatabase> {
    val context = args.first() as Context
    val dbFile = context.getDatabasePath("zeku.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}