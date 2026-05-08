package com.ghost.zeku.di


import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.AniListSource
import com.ghost.zeku.data.remote.anilist.providers.*
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.data.remote.mal.MalSource
import com.ghost.zeku.data.remote.mal.providers.*
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
            mediaList = AniListMediaProvider(
                api = get(),
                parser = get()
            ),
            mediaSearch = AniListSearchProvider(api = get(), parser = get()),
            mediaDetails = AniListDetailsProvider(api = get(), parser = get()),
            mediaTracker = AniListMediaTrackerV2(api = get(), parser = get(), authRepository = get()),
            mediaContentProvider = AniListMediaContentProvider(api = get(), parser = get()),
            userProfile = AniListUserProvider(api = get(), parser = get()),
        )
    }

    single {
        MalSource(
            mediaProvider = MalMediaProvider(
                api = get(),
                parser = get()
            ),
            mediaSearch = MalSearchProvider(api = get(), parser = get()),
            mediaDetail = MalDetailsProvider(malApi = get(), parser = get(), jikanApi = get()),
            mediaTracker = MalMediaTrackerV2(api = get(), parser = get(), authRepository = get()),
            mediaContentProvider = MalMediaContentProvider(api = get(), parser = get(), jikanApi = get()),
            userProvider = MalUserProvider(api = get(), parser = get()),
        )
    }


}