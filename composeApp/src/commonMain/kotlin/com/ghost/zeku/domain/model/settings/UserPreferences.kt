package com.ghost.zeku.domain.model.settings

import com.ghost.zeku.domain.model.enum.ProviderType
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val activeProvider: ProviderType = ProviderType.ANILIST
)