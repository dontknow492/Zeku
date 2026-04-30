package com.ghost.zeku.domain

import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.provider.*

interface MediaSource :
    AnimeListProvider,
    MangaListProvider,
    AnimeSearchProvider,
    MangaSearchProvider,
    AnimeDetailsProvider,
    MangaDetailsProvider,
    MediaTrackerProvider,
    UserProvider {
    suspend fun getProviderType(): ProviderType
}