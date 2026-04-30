package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.Serializable

// Staff
@Serializable
data class AniListStaffConnection(val edges: List<AniListStaffEdge>? = null)

@Serializable
data class AniListStaffEdge(val role: String? = null, val node: AniListStaffNode? = null)

@Serializable
data class AniListStaffNode(val id: Int, val name: AniListTitle? = null, val image: AniListCoverImage? = null)
