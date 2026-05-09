package com.ghost.zeku.domain

import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.provider.*

interface MediaSource :

    MediaListProvider,

    MediaSearchProvider,

    MediaDetailsProvider,

    MediaTrackerProviderV2,

    MediaContentProvider,

    UserProvider {

    suspend fun getProviderType(): ProviderType
}