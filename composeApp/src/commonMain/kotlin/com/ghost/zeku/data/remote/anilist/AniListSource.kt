package com.ghost.zeku.data.remote.anilist

import com.ghost.zeku.data.remote.anilist.providers.*
import com.ghost.zeku.domain.MediaSource
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.provider.*


/**
 * This class simply groups the 7 AniList workers together under one umbrella.
 * The 'by' keyword tells Kotlin to automatically pass interface calls to these injected objects.
 */
class AniListSource(
    animeList: AniListAnimeProvider,
    mangaList: AniListMangaProvider,
    animeSearch: AniListAnimeSearchProvider,
    mangaSearch: AniListMangaSearchProvider,
    animeDetails: AniListAnimeDetailsProvider,
    mangaDetails: AniListMangaDetailsProvider,
    mediaTracker: AniListMediaTracker,
    userProfile: AniListUserProvider,
) : MediaSource,
    AnimeListProvider by animeList,
    MangaListProvider by mangaList,
    AnimeSearchProvider by animeSearch,
    MangaSearchProvider by mangaSearch,
    AnimeDetailsProvider by animeDetails,
    MangaDetailsProvider by mangaDetails,
    MediaTrackerProvider by mediaTracker,
    UserProvider by userProfile {
    override suspend fun getProviderType(): ProviderType = ProviderType.ANILIST
}


