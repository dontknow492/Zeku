package com.ghost.zeku.domain.model.media

import kotlinx.serialization.Serializable

@Serializable
data class PageResult<T>(
    val items: List<T>,
    val currentPage: Int,
    val hasNextPage: Boolean,
    val totalPages: Int?,
)