package com.ghost.zeku.domain.model

import com.ghost.zeku.domain.model.enum.ProviderType

data class UserProfile(
    val id: String,
    val source: ProviderType,
    val username: String,
    val avatarUrl: String?,
    val bannerUrl: String? = null
)

