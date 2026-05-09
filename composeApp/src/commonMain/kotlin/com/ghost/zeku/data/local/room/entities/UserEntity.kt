package com.ghost.zeku.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ghost.zeku.domain.model.ProviderType

@Entity(tableName = "user_profiles")
data class UserEntity(
    @PrimaryKey
    val providerType: ProviderType,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val bannerUrl: String?
)