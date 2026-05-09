package com.ghost.zeku.domain.model.filter

enum class SortDirection(val sqlKeyword: String) {
    ASCENDING("ASC"),
    DESCENDING("DESC")
}