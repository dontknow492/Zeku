package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.Serializable

@Serializable
data class AniListPageInfo(
    val currentPage: Int,
    val hasNextPage: Boolean,
    val lastPage: Int,
    val total: Int,
)