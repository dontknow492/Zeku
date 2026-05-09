package com.ghost.zeku.domain.model

import kotlinx.serialization.Serializable

@Serializable
// Standardized types for switching
enum class ProviderType {
    ANILIST,
    MYANIMELIST
}