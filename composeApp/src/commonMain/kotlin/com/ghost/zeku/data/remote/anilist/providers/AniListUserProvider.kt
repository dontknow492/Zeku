package com.ghost.zeku.data.remote.anilist.providers

import com.ghost.zeku.data.remote.anilist.AniListApi
import com.ghost.zeku.data.remote.anilist.AniListResponseParser
import com.ghost.zeku.data.remote.anilist.toDomain
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.provider.UserProvider

class AniListUserProvider(
    private val api: AniListApi,
    private val parser: AniListResponseParser
) : UserProvider {
    override suspend fun getCurrentUser(): ApiResult<UserProfile> {
        return parser.safeApiCall(
            apiCall = { api.getCurrentUser() },
            transform = { it.viewer.toDomain() }
        )
    }
}

