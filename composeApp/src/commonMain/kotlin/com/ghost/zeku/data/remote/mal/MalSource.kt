package com.ghost.zeku.data.remote.mal

import com.ghost.zeku.data.remote.mal.providers.*
import com.ghost.zeku.domain.MediaSource
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.provider.*

class MalSource(
    mediaProvider: MalMediaProvider,
    mediaSearch: MalSearchProvider,
    mediaDetail: MalDetailsProvider,
    mediaTracker: MalMediaTrackerV2,
    mediaContentProvider: MalMediaContentProvider,
    userProvider: MalUserProvider,
) : MediaSource,
    MediaListProvider by mediaProvider,
    MediaSearchProvider by mediaSearch,
    MediaDetailsProvider by mediaDetail,
    MediaTrackerProviderV2 by mediaTracker,
    MediaContentProvider by mediaContentProvider,
    UserProvider by userProvider {
    override suspend fun getProviderType(): ProviderType = ProviderType.MYANIMELIST
}