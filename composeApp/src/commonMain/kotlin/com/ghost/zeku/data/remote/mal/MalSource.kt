package com.ghost.zeku.data.remote.mal

import com.ghost.zeku.data.remote.mal.providers.*
import com.ghost.zeku.domain.MediaSource
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.provider.*

class MalSource(
    animeList: MalAnimeProvider,
    mangaList: MalMangaProvider,
    animeSearch: MalAnimeSearchProvider,
    mangaSearch: MalMangaSearchProvider,
    animeDetails: MalAnimeDetailsProvider,
    mangaDetails: MalMangaDetailsProvider,
    mediaTracker: MalMediaTracker,
) : MediaSource,
    AnimeListProvider by animeList,
    MangaListProvider by mangaList,
    AnimeSearchProvider by animeSearch,
    MangaSearchProvider by mangaSearch,
    AnimeDetailsProvider by animeDetails,
    MangaDetailsProvider by mangaDetails,
    MediaTrackerProvider by mediaTracker {
    override suspend fun getProviderType(): ProviderType = ProviderType.MYANIMELIST
}