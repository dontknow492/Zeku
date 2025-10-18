package org.ghost.zeku.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ghost.zeku.database.DBManager
import org.ghost.zeku.database.dao.CommandTemplateDao
import org.ghost.zeku.database.dao.CookieDao
import org.ghost.zeku.database.dao.DownloadDao
import org.ghost.zeku.database.dao.HistoryDao
import org.ghost.zeku.database.dao.LogDao
import org.ghost.zeku.database.dao.TerminalDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDBManager(context: Context): DBManager {
        return DBManager.getInstance(context)
    }

    @Provides
    fun provideHistoryDao(dbManager: DBManager): HistoryDao {
        return dbManager.historyDao()
    }

    @Provides
    fun provideDownloadDao(dbManager: DBManager): DownloadDao {
        return dbManager.downloadDao()
    }

    @Provides
    fun provideCommandTemplateDao(dbManager: DBManager): CommandTemplateDao {
        return dbManager.commandTemplateDao()
    }

    @Provides
    fun provideCookieDao(dbManager: DBManager): CookieDao {
        return dbManager.cookieDao()
    }

    @Provides
    fun provideLogDao(dbManager: DBManager): LogDao {
        return dbManager.logDao()
    }

    @Provides
    fun provideTerminalDao(dbManager: DBManager): TerminalDao {
        return dbManager.terminalDao()
    }
}