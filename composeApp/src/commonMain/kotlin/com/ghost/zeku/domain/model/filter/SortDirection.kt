package com.ghost.zeku.domain.model.filter

import kotlinx.serialization.Serializable

@Serializable
enum class SortDirection(val sqlKeyword: String) {
    ASCENDING("ASC"),
    DESCENDING("DESC")
}