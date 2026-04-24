package com.ghost.zeku.data.repository


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
    private val httpClient: HttpClient, // We need Ktor to exchange the code!
    private val clientId: String
) : ProviderAuthRepository {

    override val providerType: ProviderType = ProviderType.MYANIMELIST

    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_token_mal_access"
        private const val KEY_REFRESH_TOKEN = "auth_token_mal_refresh"
        private const val KEY_CODE_VERIFIER = "auth_mal_code_verifier" // Temporarily holds the PKCE secret
    }

    private val _authState = MutableStateFlow<AuthState>(
        if (settings.getString(KEY_ACCESS_TOKEN, "").isNotEmpty()) AuthState.LoggedIn else AuthState.LoggedOut
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun isUserLoggedIn(): Boolean = _authState.value == AuthState.LoggedIn

    override fun getAuthorizationUrl(): String {
        // 1. Generate PKCE Verifier
        val verifier = generateCodeVerifier()
        settings.putString(KEY_CODE_VERIFIER, verifier) // Save it for when the browser returns!

        // MAL requires code_challenge to be exactly the same as code_verifier (plain challenge method)
        return "https://myanimelist.net/v1/oauth2/authorize?response_type=code&client_id=$clientId&code_challenge=$verifier"
    }

    override suspend fun handleAuthRedirectUri(uriString: String): ApiResult<Unit> {
        Napier.v { "Handling MAL auth redirect..." }
        _authState.update { AuthState.Loading }

        return try {
            // MAL returns the code in the query params: myapp://auth?code=abc123...
            val url = Url(uriString)
            val code = url.parameters["code"]
            val verifier = settings.getString(KEY_CODE_VERIFIER, "")

            if (code == null) {
                throw IllegalStateException("MAL URI did not contain a 'code' parameter.")
            }
            if (verifier.isEmpty()) {
                throw IllegalStateException("Code verifier was lost. Cannot complete PKCE flow.")
            }

            // Execute the token exchange with Ktor
            val response: MalTokenResponse = httpClient.submitForm(
                url = "https://myanimelist.net/v1/oauth2/token",
                formParameters = parameters {
                    append("client_id", clientId)
                    append("code", code)
                    append("code_verifier", verifier)
                    append("grant_type", "authorization_code")
                }
            ).body()

            // Save the tokens securely
            settings.putString(KEY_ACCESS_TOKEN, response.accessToken)
            settings.putString(KEY_REFRESH_TOKEN, response.refreshToken)
            settings.remove(KEY_CODE_VERIFIER) // Cleanup the temp secret

            _authState.update { AuthState.LoggedIn }
            Napier.i { "MyAnimeList login successful!" }
            ApiResult.Success(Unit)

        } catch (e: Exception) {
            Napier.e(e) { "Error exchanging MAL code for token." }
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
        Napier.i { "Logging out of MAL" }
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        _authState.update { AuthState.LoggedOut }
    }

    override suspend fun getAccessToken(): String? {
        // Future Upgrade: Check expiration timestamp here, and use refreshToken if expired!
        return settings.getString(KEY_ACCESS_TOKEN, "").takeIf { it.isNotEmpty() }
    }

    /**
     * Generates a 128-character random string required for OAuth2 PKCE security.
     */
    private fun generateCodeVerifier(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..128).map { allowedChars.random() }.joinToString("")
    }
}









