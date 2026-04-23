package com.ghost.zeku.data.repository

import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import zeku.composeApp.BuildConfig

class AniListAuthRepositoryImpl : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun getAuthorizationUrl(): String {
        // You'll replace this with your actual client ID from AniList developer settings
        val clientId = "YOUR_ANILIST_CLIENT_ID"
        val redirectUri = "zeku://auth"
        return "https://anilist.co/api/v2/oauth/authorize?client_id=$clientId&redirect_uri=$redirectUri&response_type=code"
    }

    override suspend fun getAccessToken(): String? {
        if (BuildConfig.IS_DEBUG) {
            val token = BuildConfig.ANILIST_TOKEN
            return token
        }

        val state = authState.value
        return if (state is AuthState.Authenticated) state.accessToken else null
    }

    override suspend fun handleAuthorizationCode(code: String): Result<Unit> {
        return try {
            // Here you would call your AniListAuthApi to exchange the code for a token
            // val response = authApi.exchangeCodeForToken(...)
            // val token = response.access_token

            // For now, we simulate success with a placeholder
            val token = "mock_token"
            _authState.value = AuthState.Authenticated(token)

            // TODO: Save token to secure local storage (Settings/DataStore)
            Result.success(Unit)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Auth Failed")
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        _authState.value = AuthState.Unauthenticated
        // TODO: Clear token from secure local storage
    }
}