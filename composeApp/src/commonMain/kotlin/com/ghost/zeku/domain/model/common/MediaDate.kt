package com.ghost.zeku.domain.model.common


import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable


@Serializable
data class MediaDate(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null
)


fun MediaDate.format(): String {
    return when {
        year == null -> "Unknown"
        month == null -> "$year"
        day == null -> "$month/$year"
        else -> "$day/$month/$year"
    }
}


fun MediaDate.toLocalDateOrNull(): LocalDate? {
    TODO("Not yet implemented")
}