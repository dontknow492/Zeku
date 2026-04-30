package com.ghost.zeku.data.remote.anilist.model

import kotlinx.serialization.Serializable

// Studios
@Serializable
data class AniListStudioConnection(val edges: List<AniListStudioEdge>? = null)

@Serializable
data class AniListStudioEdge(val isMain: Boolean? = null, val node: AniListStudioNode? = null)

@Serializable
data class AniListStudioNode(val id: Int, val name: String, val isAnimationStudio: Boolean)
