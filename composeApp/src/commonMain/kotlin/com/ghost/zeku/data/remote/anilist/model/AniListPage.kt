package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AniListPageData(
    @SerialName("Page") val page: AniListPage?
)

@Serializable
data class AniListPage(
    val pageInfo: AniListPageInfo,
    @SerialName("media")
    val media: List<AniListMedia>?,
)