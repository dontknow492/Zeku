package com.ghost.zeku.data.remote.mal

import com.ghost.zeku.data.remote.mal.provider.MalMangaDetailsProvider
import com.ghost.zeku.data.remote.mal.provider.MalMangaProvider
import com.ghost.zeku.data.remote.mal.provider.MalMangaSearchProvider
import com.ghost.zeku.data.remote.mal.providers.MalAnimeDetailsProvider
import com.ghost.zeku.data.remote.mal.providers.MalAnimeProvider
import com.ghost.zeku.data.remote.mal.providers.MalAnimeSearchProvider
import com.ghost.zeku.data.remote.mal.providers.MalMediaTracker
import com.ghost.zeku.domain.MediaSource
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
    MediaTrackerProvider by mediaTracker