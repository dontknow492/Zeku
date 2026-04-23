package com.ghost.zeku.domain

import com.ghost.zeku.domain.provider.*

interface MediaSource :
    AnimeListProvider,
    MangaListProvider,
    AnimeSearchProvider,
    MangaSearchProvider,
    AnimeDetailsProvider,
    MangaDetailsProvider,
    MediaTrackerProvider