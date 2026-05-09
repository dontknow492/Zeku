package com.ghost.zeku.data.remote.anilist

import com.ghost.zeku.data.remote.anilist.providers.*
import com.ghost.zeku.domain.MediaSource
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.provider.*


/**
 * This class simply groups the 7 AniList workers together under one umbrella.
 * The 'by' keyword tells Kotlin to automatically pass interface calls to these injected objects.
 */
class AniListSource(
    mediaList: AniListMediaProvider,
    mediaSearch: AniListSearchProvider,
    mediaDetails: AniListDetailsProvider,
    mediaTracker: AniListMediaTrackerV2,
    userProfile: AniListUserProvider,
    mediaContentProvider: AniListMediaContentProvider
) : MediaSource,
    MediaListProvider by mediaList,
    MediaSearchProvider by mediaSearch,
    MediaDetailsProvider by mediaDetails,
    MediaTrackerProviderV2 by mediaTracker,
    MediaContentProvider by mediaContentProvider,
    UserProvider by userProfile {
    override suspend fun getProviderType(): ProviderType = ProviderType.ANILIST
}


