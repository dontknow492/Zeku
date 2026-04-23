package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AniListResponse<T>(
    val data: T? = null,
    val errors: List<AniListError>? = null  // Add errors field
)


// --- Update Response ---
@Serializable
data class AniListUpdateData(
    @SerialName("SaveMediaListEntry") val entry: AniListMediaListEntry?
)

// --- Delete Response ---
@Serializable
data class AniListDeleteData(
    @SerialName("DeleteMediaListEntry") val result: AniListDeleteResult?
)

@Serializable
data class AniListDeleteResult(
    val deleted: Boolean
)


@Serializable
data class AniListError(
    val message: String,
    val status: Int? = null,
    val locations: List<AniListErrorLocation>? = null,
    val path: List<String>? = null
)

@Serializable
data class AniListErrorLocation(
    val line: Int,
    val column: Int
)