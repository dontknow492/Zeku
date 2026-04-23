package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AniListUserPageData(
    @SerialName("Page") val page: AniListPageMediaList?
)

@Serializable
data class AniListPageMediaList(
    val pageInfo: AniListPageInfo?,
    // renamed to mediaList to match GraphQL field exactly
    val mediaList: List<AniListMediaListEntry>?
)



