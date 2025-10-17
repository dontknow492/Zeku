package org.ghost.zeku.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ghost.zeku.database.dao.CommandTemplateDao
import org.ghost.zeku.database.dao.CookieDao
import org.ghost.zeku.database.dao.DownloadDao
import org.ghost.zeku.database.dao.HistoryDao
import org.ghost.zeku.database.dao.LogDao
import org.ghost.zeku.database.dao.TerminalDao
import org.ghost.zeku.database.models.CommandTemplate
import org.ghost.zeku.database.models.CookieItem
import org.ghost.zeku.database.models.DownloadItem
import org.ghost.zeku.database.models.HistoryItem
import org.ghost.zeku.database.models.LogItem
import org.ghost.zeku.database.models.TemplateShortcut
import org.ghost.zeku.database.models.TerminalItem


@TypeConverters(Converters::class)
@Database(
    entities = [
        HistoryItem::class,
        DownloadItem::class,
        CommandTemplate::class,
        CookieItem::class,
        LogItem::class,
        TerminalItem::class,
        TemplateShortcut::class,
    ],
    version = 1
)
abstract class DBManager : RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun commandTemplateDao(): CommandTemplateDao
    abstract fun cookieDao(): CookieDao
    abstract fun logDao(): LogDao
    abstract fun terminalDao(): TerminalDao


    companion object {
        //prevents multiple instances of db getting created at the same time
        @Volatile
        private var instance: DBManager? = null

        //if its not null return it, otherwise create db
        fun getInstance(context: Context): DBManager {
            return instance ?: synchronized(this) {

                val dbInstance = Room.databaseBuilder(
                    context.applicationContext,
                    DBManager::class.java,
                    "ZekuDatabase"
                )
                    .build()
                instance = dbInstance
                dbInstance
            }
        }

    }
}