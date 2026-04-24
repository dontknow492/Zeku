package com.ghost.zeku.data.repository

import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.model.api.ErrorType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.ProviderAuthRepository
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class AniListAuthRepositoryImpl(
    private val settings: Settings,
    private val clientId: String
) : ProviderAuthRepository {

    override val providerType: ProviderType = ProviderType.ANILIST

    companion object {
        private const val KEY_TOKEN = "auth_token_anilist"
    }

    private val _authState = MutableStateFlow<AuthState>(
        if (settings.getString(KEY_TOKEN, "").isNotEmpty()) AuthState.LoggedIn else AuthState.LoggedOut
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun isUserLoggedIn(): Boolean = _authState.value == AuthState.LoggedIn

    override fun getAuthorizationUrl(): String {
        return "https://anilist.co/api/v2/oauth/authorize?client_id=$clientId&response_type=token"
    }

    override suspend fun handleAuthRedirectUri(uriString: String): ApiResult<Unit> {
        Napier.v { "Handling AniList auth redirect..." }
        _authState.update { AuthState.Loading }

        return try {
            // AniList appends the token as a hash fragment: myapp://auth#access_token=abc123...
            val tokenRegex = "access_token=([^&]+)".toRegex()
            val match = tokenRegex.find(uriString)
            val token = match?.groupValues?.get(1)

            if (token != null) {
                settings.putString(KEY_TOKEN, token)
                _authState.update { AuthState.LoggedIn }
                Napier.i { "AniList login successful!" }
                ApiResult.Success(Unit)
            } else {
                _authState.update { AuthState.LoggedOut }
                ApiResult.Error(
                    error = ApiError(
                        type = ErrorType.PARSE_ERROR,
                        message = "Could not extract access token from AniList URI.",
                        rawError = uriString,
                        recoverable = true,
                        recoverySuggestion = "Please try logging in again."
                    )
                )
            }
        } catch (e: Exception) {
            Napier.e(e) { "Error handling AniList auth redirect." }
            _authState.update { AuthState.LoggedOut }
            ApiResult.Error(
                error = ApiError(
                    type = ErrorType.UNKNOWN,
                    message = e.message ?: "An unexpected error occurred during AniList login.",
                    cause = e,
                    recoverable = true
                )
            )
        }
    }

    override suspend fun logout() {
        Napier.i { "Logging out of AniList" }
        settings.remove(KEY_TOKEN)
        _authState.update { AuthState.LoggedOut }
    }

    override suspend fun getAccessToken(): String? {
        return settings.getString(KEY_TOKEN, "").takeIf { it.isNotEmpty() }
    }
}