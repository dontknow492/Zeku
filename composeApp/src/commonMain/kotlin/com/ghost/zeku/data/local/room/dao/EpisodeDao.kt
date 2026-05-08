package com.ghost.zeku.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.EpisodeEntity
import com.ghost.zeku.domain.model.enum.ProviderType
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Upsert
    suspend fun upsertAll(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes WHERE mediaId = :mediaId AND provider = :source ORDER BY number ASC")
    fun getEpisodesByMedia(mediaId: Int, source: ProviderType): PagingSource<Int, EpisodeEntity>

    // Useful for a "Downloads" screen to see everything currently saved on the device
    @Query("SELECT * FROM episodes WHERE downloadStatus = 'COMPLETED' ORDER BY mediaId ASC, number ASC")
    fun getAllDownloadedEpisodes(): Flow<List<EpisodeEntity>>

    @Query("DELETE FROM episodes WHERE mediaId = :mediaId AND provider = :source")
    suspend fun clearEpisodesForMedia(mediaId: Int, source: ProviderType)

    // Synchronous fetch to preserve local state (downloads, watch progress) during API refreshes
    @Query("SELECT * FROM episodes WHERE mediaId = :mediaId AND provider = :source")
    suspend fun getEpisodesForMediaSync(mediaId: Int, source: ProviderType): List<EpisodeEntity>

}

