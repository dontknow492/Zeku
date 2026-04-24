package com.ghost.zeku.data.repository.auth

import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.model.api.ErrorType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.ProviderAuthRepository
import com.russhwolf.settings.Settings
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class MalTokenResponse(
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)

class MalAuthRepositoryImpl(
    private val settings: Settings,
    private val httpClient: HttpClient,
    private val clientId: String,
    private val redirectUri: String,
) : ProviderAuthRepository {

    override val providerType: ProviderType = ProviderType.MYANIMELIST

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_token_mal_access"
        private const val KEY_REFRESH_TOKEN = "auth_token_mal_refresh"
        private const val KEY_CODE_VERIFIER = "auth_mal_code_verifier"
    }

    private val _authState = MutableStateFlow<AuthState>(
        if (settings.getString(KEY_ACCESS_TOKEN, "").isNotEmpty()) AuthState.LoggedIn else AuthState.LoggedOut
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val initialState = if (settings.getString(KEY_ACCESS_TOKEN, "").isNotEmpty()) "LoggedIn" else "LoggedOut"
        val hasRefreshToken = settings.getString(KEY_REFRESH_TOKEN, "").isNotEmpty()
        Napier.i {
            "MALAuthRepository initialized: state=$initialState, " +
                    "accessTokenPresent=${initialState == "LoggedIn"}, " +
                    "refreshTokenPresent=$hasRefreshToken, " +
                    "clientId=${clientId.take(4)}****, " +
                    "redirectUri=$redirectUri"
        }
    }

    override fun isUserLoggedIn(): Boolean {
        val loggedIn = _authState.value == AuthState.LoggedIn
        Napier.v { "MAL isUserLoggedIn: $loggedIn" }
        return loggedIn
    }

    override fun getAuthorizationUrl(): String {
        Napier.d { "MAL authorization URL generation started" }
        val verifier = generateCodeVerifier()
        settings.putString(KEY_CODE_VERIFIER, verifier)
        Napier.v { "MAL PKCE code verifier generated and stored (length=${verifier.length})" }

        val url = "https://myanimelist.net/v1/oauth2/authorize?" +
                "response_type=code&" +
                "client_id=$clientId&" +
                "code_challenge=$verifier&" +
                "redirect_uri=$redirectUri"
        Napier.d { "MAL authorization URL generated" }
        Napier.v { "MAL auth URL: ${url}..." }
        return url
    }

    override suspend fun handleAuthRedirectUri(uriString: String): ApiResult<Unit> {
        Napier.d { "MAL handleAuthRedirectUri called" }
        Napier.v { "MAL redirect URI received: ${uriString.take(100)}..." }
        _authState.update { AuthState.Loading }

        return try {
            val url = Url(uriString)
            val code = url.parameters["code"]
            val verifier = settings.getString(KEY_CODE_VERIFIER, "")

            if (code == null) {
                Napier.w { "MAL redirect URI missing 'code' parameter" }
                throw IllegalStateException("MAL URI did not contain a 'code' parameter.")
            }
            if (verifier.isEmpty()) {
                Napier.e { "MAL PKCE code verifier missing from storage" }
                throw IllegalStateException("Code verifier was lost. Cannot complete PKCE flow.")
            }

            Napier.v { "MAL exchanging authorization code for token (code=${code.take(4)}****)" }


            val rawResponse = httpClient.submitForm(
                url = "https://myanimelist.net/v1/oauth2/token",
                formParameters = parameters {
                    append("client_id", clientId)
                    append("code", code)
                    append("code_verifier", verifier)
                    append("grant_type", "authorization_code")
                    append("redirect_uri", redirectUri)
                },
            )

            Napier.v { "MAL token exchanged by code: ${rawResponse.status}" }
            val response = rawResponse.body<MalTokenResponse>()


            settings.putString(KEY_ACCESS_TOKEN, response.accessToken)
            settings.putString(KEY_REFRESH_TOKEN, response.refreshToken)
            settings.remove(KEY_CODE_VERIFIER)

            _authState.update { AuthState.LoggedIn }
            Napier.i {
                "MAL login successful: tokenType=${response.tokenType}, " +
                        "expiresIn=${response.expiresIn}s, " +
                        "accessToken=${response.accessToken.take(4)}****, " +
                        "refreshToken=${response.refreshToken.take(4)}****"
            }
            ApiResult.Success(Unit)

        } catch (e: Exception) {
            Napier.e(e) { "MAL token exchange failed" }
            _authState.update { AuthState.LoggedOut }
            ApiResult.Error(
                error = ApiError(
                    type = ErrorType.UNAUTHORIZED,
                    message = "Failed to authenticate with MyAnimeList.",
                    cause = e,
                    recoverable = true,
                    recoverySuggestion = "Please check your internet connection and try logging in again."
                )
            )
        }
    }

    override suspend fun logout() {
        Napier.d { "MAL logout initiated" }
        val hadAccessToken = settings.getString(KEY_ACCESS_TOKEN, "").isNotEmpty()
        val hadRefreshToken = settings.getString(KEY_REFRESH_TOKEN, "").isNotEmpty()
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        _authState.update { AuthState.LoggedOut }
        Napier.i {
            "MAL logout complete: accessTokenCleared=$hadAccessToken, refreshTokenCleared=$hadRefreshToken"
        }
    }

    override suspend fun getAccessToken(): String? {
        val token = settings.getString(KEY_ACCESS_TOKEN, "").takeIf { it.isNotEmpty() }
        if (token != null) {
            Napier.v { "MAL access token retrieved: ${token.take(4)}****" }
        } else {
            Napier.v { "MAL access token requested but not found" }
        }
        return token
    }

    /**
     * Generates a 128-character random string required for OAuth2 PKCE security.
     */
    private fun generateCodeVerifier(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..128).map { allowedChars.random() }.joinToString("")
    }
}