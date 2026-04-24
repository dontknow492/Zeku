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
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class AniListTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("expires_in") val expiresIn: Long = 0,
    @SerialName("refresh_token") val refreshToken: String? = null
)

class AniListAuthRepositoryImpl(
    private val settings: Settings,
    private val httpClient: HttpClient,
    private val clientId: String,
    private val clientSecret: String,
    private val redirectUri: String,
) : ProviderAuthRepository {

    override val providerType: ProviderType = ProviderType.ANILIST

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_token_anilist"
        private const val KEY_REFRESH_TOKEN = "auth_token_anilist_refresh"
        private const val KEY_TOKEN_DATE = "auth_token_anilist_date"
    }

    private val _authState = MutableStateFlow<AuthState>(
        if (settings.getString(KEY_ACCESS_TOKEN, "").isNotEmpty()) AuthState.LoggedIn else AuthState.LoggedOut
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val initialState = if (settings.getString(KEY_ACCESS_TOKEN, "").isNotEmpty()) "LoggedIn" else "LoggedOut"
        Napier.i {
            "AniListAuthRepository initialized: state=$initialState, " +
                    "clientId=${clientId.take(4)}****, " +
                    "hasClientSecret=${clientSecret.isNotEmpty()}, " +
                    "redirectUri=$redirectUri"
        }
    }

    override fun isUserLoggedIn(): Boolean {
        val loggedIn = _authState.value == AuthState.LoggedIn
        Napier.v { "AniList isUserLoggedIn: $loggedIn" }
        return loggedIn
    }

    override fun getAuthorizationUrl(): String {
        val url = "https://anilist.co/api/v2/oauth/authorize?" +
                "client_id=$clientId&" +
                "redirect_uri=$redirectUri&" +
                "response_type=code"
        Napier.d { "AniList authorization URL generated (Authorization Code flow)" }
        Napier.v { "AniList auth URL: ${url.take(80)}..." }
        return url
    }

    override suspend fun handleAuthRedirectUri(uriString: String): ApiResult<Unit> {
        Napier.d { "AniList handleAuthRedirectUri called" }
        Napier.v { "AniList redirect URI received: ${uriString.take(150)}..." }
        _authState.update { AuthState.Loading }

        return try {
            // Extract the authorization CODE (not token!)
            val code = extractAuthorizationCode(uriString)

            if (code == null) {
                Napier.w { "AniList auth redirect: could not extract authorization code from URI" }
                Napier.d { "Full URI that failed parsing: $uriString" }
                _authState.update { AuthState.LoggedOut }
                return ApiResult.Error(
                    error = ApiError(
                        type = ErrorType.PARSE_ERROR,
                        message = "Could not extract authorization code from AniList redirect.",
                        rawError = uriString,
                        recoverable = true,
                        recoverySuggestion = "Please try logging in again."
                    )
                )
            }

            Napier.v { "AniList authorization code extracted: ${code.take(4)}****" }

            // Exchange the code for an access token
            val tokenResponse = exchangeCodeForToken(code)

            // Save tokens
            settings.putString(KEY_ACCESS_TOKEN, tokenResponse.accessToken)
            settings.putLong(KEY_TOKEN_DATE, System.currentTimeMillis())

            if (tokenResponse.refreshToken != null) {
                settings.putString(KEY_REFRESH_TOKEN, tokenResponse.refreshToken)
                Napier.d { "AniList refresh token saved" }
            }

            _authState.update { AuthState.LoggedIn }
            Napier.i {
                "AniList login successful: " +
                        "tokenType=${tokenResponse.tokenType}, " +
                        "expiresIn=${tokenResponse.expiresIn}s, " +
                        "accessToken=${tokenResponse.accessToken.take(8)}****${tokenResponse.accessToken.takeLast(4)}"
            }

            // Log token info for debugging
            val tokenParts = tokenResponse.accessToken.split(".")
            if (tokenParts.size == 3) {
                Napier.d { "AniList token appears to be JWT format (${tokenResponse.accessToken.length} chars)" }
            } else {
                Napier.d { "AniList token format: ${tokenResponse.accessToken.length} characters" }
            }

            ApiResult.Success(Unit)

        } catch (e: Exception) {
            Napier.e(e) { "AniList auth code exchange failed" }
            _authState.update { AuthState.LoggedOut }
            ApiResult.Error(
                error = ApiError(
                    type = ErrorType.UNAUTHORIZED,
                    message = "Failed to authenticate with AniList: ${e.message}",
                    cause = e,
                    recoverable = true,
                    recoverySuggestion = "Please check your internet connection and try again."
                )
            )
        }
    }

    /**
     * Exchange the authorization code for an access token.
     * AniList token endpoint: POST https://anilist.co/api/v2/oauth/token
     */
    private suspend fun exchangeCodeForToken(code: String): AniListTokenResponse {
        Napier.d { "Exchanging AniList authorization code for token" }
        Napier.v { "Token exchange: code=${code.take(4)}****, clientId=${clientId.take(4)}****" }

        val response = httpClient.submitForm(
            url = "https://anilist.co/api/v2/oauth/token",
            formParameters = parameters {
                append("grant_type", "authorization_code")
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("code", code)
                append("redirect_uri", redirectUri)
            }
        )

        val responseBody = response.bodyAsText()
        Napier.v { "AniList token exchange response: status=${response.status}, body=${responseBody.take(200)}" }

        if (!response.status.isSuccess()) {
            Napier.e { "AniList token exchange failed with status ${response.status}: $responseBody" }
            throw IllegalStateException("Token exchange failed: $responseBody")
        }

        val tokenResponse = response.body<AniListTokenResponse>()
        Napier.i { "AniList token exchange successful" }
        return tokenResponse
    }

    /**
     * Extracts the authorization CODE from the redirect URI.
     * With response_type=code, the format is:
     * com.ghost.zeku://auth?code=abc123...
     * or
     * http://localhost:8080/?code=abc123...
     */
    private fun extractAuthorizationCode(uriString: String): String? {
        Napier.v { "Extracting authorization code from AniList redirect" }

        // Method 1: Try parsing as URL (handles both custom schemes and HTTP)
        try {
            val url = Url(uriString)
            val code = url.parameters["code"]
            if (code != null) {
                Napier.d { "Authorization code extracted via URL parsing" }
                return code
            }
        } catch (e: Exception) {
            Napier.v { "URL parsing failed, trying regex fallback: ${e.message}" }
        }

        // Method 2: Regex fallback
        val codePattern = "[?&]code=([^&\\s]+)".toRegex()
        val match = codePattern.find(uriString)
        if (match != null) {
            val code = match.groupValues[1]
            Napier.d { "Authorization code extracted via regex" }
            return code
        }

        Napier.w { "No authorization code found in redirect URI" }
        return null
    }

    override suspend fun logout() {
        Napier.d { "AniList logout initiated" }
        val hadToken = settings.getString(KEY_ACCESS_TOKEN, "").isNotEmpty()
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_TOKEN_DATE)
        _authState.update { AuthState.LoggedOut }
        Napier.i { "AniList logout complete: tokenCleared=$hadToken" }
    }

    override suspend fun getAccessToken(): String? {
        val token = settings.getString(KEY_ACCESS_TOKEN, "").takeIf { it.isNotEmpty() }
        if (token != null) {
            Napier.v { "AniList access token retrieved: ${token.take(4)}****" }
        } else {
            Napier.v { "AniList access token requested but not found" }
        }
        return token
    }
}