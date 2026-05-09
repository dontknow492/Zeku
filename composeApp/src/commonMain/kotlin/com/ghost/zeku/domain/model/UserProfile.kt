package com.ghost.zeku.domain.model

data class UserProfile(
    val id: String,
    val source: ProviderType,
    val username: String,
    val avatarUrl: String?,
    val bannerUrl: String? = null
)

