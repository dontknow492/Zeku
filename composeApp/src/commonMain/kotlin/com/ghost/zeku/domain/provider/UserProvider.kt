package com.ghost.zeku.domain.provider

import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.api.ApiResult


interface UserProvider {
    suspend fun getCurrentUser(): ApiResult<UserProfile>
}