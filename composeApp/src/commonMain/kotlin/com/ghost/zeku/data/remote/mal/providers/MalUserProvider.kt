package com.ghost.zeku.data.remote.mal.providers

import com.ghost.zeku.data.remote.mal.MalApi
import com.ghost.zeku.data.remote.mal.MalResponseParser
import com.ghost.zeku.data.remote.mal.toDomain
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.provider.UserProvider


class MalUserProvider(
    private val api: MalApi,
    private val parser: MalResponseParser
) : UserProvider {
    override suspend fun getCurrentUser(): ApiResult<UserProfile> {
        return parser.safeApiCall(
            apiCall = {
                api.getCurrentUser()
            },
            transform = { response ->
                response.toDomain()
            }
        )
    }
}


