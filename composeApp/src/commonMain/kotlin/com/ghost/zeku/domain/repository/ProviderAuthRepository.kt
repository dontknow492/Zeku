package com.ghost.zeku.domain.repository

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.model.ProviderType
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for a specific platform's authentication logic (AniList, MAL, etc.).
 */
interface ProviderAuthRepository {
    val providerType: ProviderType

    val authState: StateFlow<AuthState>

    fun isUserLoggedIn(): Boolean

    fun getUserId(): Int

    fun getAuthorizationUrl(): String

    suspend fun handleAuthRedirectUri(authCode: String): ApiResult<Unit>

    suspend fun logout()

    suspend fun getAccessToken(): String?
}