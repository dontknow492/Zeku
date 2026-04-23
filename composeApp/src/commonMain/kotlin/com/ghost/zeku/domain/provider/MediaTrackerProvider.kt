package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.enum.TrackStatus
import com.ghost.zeku.domain.model.media.Anime
import com.ghost.zeku.domain.model.media.Manga
import com.ghost.zeku.domain.model.media.PageResult

interface MediaTrackerProvider {

    suspend fun getUserAnimeList(
        userId: Int,
        status: TrackStatus,
        page: Int = 1,
        perPage: Int = 20,
        accessToken: String,
    ): ApiResult<PageResult<Anime>>

    suspend fun getUserMangaList(
        userId: Int,
        status: TrackStatus,
        page: Int = 1,
        perPage: Int = 20,
        accessToken: String,
    ): ApiResult<PageResult<Manga>>


    /**
     * Updates an entry's progress, status, or score.
     * If the entry doesn't exist yet for this mediaId, it will create it.
     */
    suspend fun updateMediaListEntry(
        accessToken: String,
        mediaId: Int,
        progress: Int? = null,
        status: TrackStatus? = null,
        score: Double? = null
    ): ApiResult<TrackEntry>

    /**
     * Removes an entry from the user's list entirely.
     */
    suspend fun deleteMediaListEntry(
        accessToken: String,
        entryId: Int
    ): Boolean
}


