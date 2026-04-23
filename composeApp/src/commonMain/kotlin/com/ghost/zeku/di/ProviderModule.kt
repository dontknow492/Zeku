package com.ghost.zeku.di


import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.AniListSource
import com.ghost.zeku.data.remote.anilist.providers.*
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.data.remote.mal.MalSource
import com.ghost.zeku.data.remote.mal.provider.MalMangaDetailsProvider
import com.ghost.zeku.data.remote.mal.provider.MalMangaProvider
import com.ghost.zeku.data.remote.mal.provider.MalMangaSearchProvider
import com.ghost.zeku.data.remote.mal.providers.MalAnimeDetailsProvider
import com.ghost.zeku.data.remote.mal.providers.MalAnimeProvider
import com.ghost.zeku.data.remote.mal.providers.MalAnimeSearchProvider
import com.ghost.zeku.data.remote.mal.providers.MalMediaTracker
import org.koin.dsl.module


val providerModule = module {

    single {
        AniListResponseParser()
    }

    single {
        MalResponseParser()
    }

    // 1. Register the complete AniList Bundle
    // We instantiate the concrete workers directly inside the bundle to save space.
    single {
        AniListSource(
            animeList = AniListAnimeProvider(api = get(), authRepository = get(), parser = get()),
            mangaList = AniListMangaProvider(api = get(), authRepository = get(), parser = get()),
            animeSearch = AniListAnimeSearchProvider(api = get(), authRepository = get(), parser = get()),
            mangaSearch = AniListMangaSearchProvider(api = get(), authRepository = get(), parser = get()),
            animeDetails = AniListAnimeDetailsProvider(api = get(), authRepository = get(), parser = get()),
            mangaDetails = AniListMangaDetailsProvider(api = get(), authRepository = get(), parser = get()),
            mediaTracker = AniListMediaTracker(aniListApi = get(), parser = get())
        )
    }

    single {
        MalSource(
            animeList = MalAnimeProvider(api = get(), authRepository = get(), parser = get()),
            mangaList = MalMangaProvider(api = get(), authRepository = get(), parser = get()),
            animeSearch = MalAnimeSearchProvider(api = get(), authRepository = get(), parser = get()),
            mangaSearch = MalMangaSearchProvider(api = get(), authRepository = get(), parser = get()),
            animeDetails = MalAnimeDetailsProvider(
                authRepository = get(),
                parser = get(),
                malApi = get(),
                jikanApi = get(),
            ),
            mangaDetails = MalMangaDetailsProvider(
                authRepository = get(),
                parser = get(),
                malApi = get(),
                jikanApi = get()
            ),
            mediaTracker = MalMediaTracker(api = get(), parser = get())
        )
    }


}