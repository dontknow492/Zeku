package com.ghost.zeku.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.zeku.data.local.room.entities.ChapterEntity
import com.ghost.zeku.domain.model.enum.ProviderType

@Dao
interface ChapterDao {
    @Upsert
    suspend fun upsertAll(chapters: List<ChapterEntity>)

    // Usually users read chapters in descending order (newest first), but you can change ASC/DESC
    @Query("SELECT * FROM chapters WHERE mediaId = :mediaId AND provider = :source ORDER BY number DESC")
    fun getChaptersByMedia(mediaId: Int, source: ProviderType): PagingSource<Int, ChapterEntity>

    @Query("DELETE FROM chapters WHERE mediaId = :mediaId AND provider = :source")
    suspend fun clearChaptersForMedia(mediaId: Int, source: ProviderType)

    @Query("SELECT * FROM chapters WHERE mediaId = :mediaId AND provider = :source")
    suspend fun getChaptersForMediaSync(mediaId: Int, source: ProviderType): List<ChapterEntity>


}