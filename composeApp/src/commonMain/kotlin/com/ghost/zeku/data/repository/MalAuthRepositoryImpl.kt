package com.ghost.zeku.data.repository


import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import zeku.composeApp.BuildConfig
import kotlin.random.Random

class MalAuthRepositoryImpl : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // PKCE requires us to temporarily hold the verifier between generating the URL
    // and handling the callback.
    // TODO: For production, consider saving this in SavedStateHandle or DataStore
    // in case the app is killed by the OS while the browser is open.
    private var currentCodeVerifier: String? = null

    override fun getAuthorizationUrl(): String {
        val clientId = "YOUR_MAL_CLIENT_ID" // Replace with your MAL Client ID

        // 1. Generate the PKCE code verifier (43-128 random characters)
        val codeVerifier = generateCodeVerifier()
        currentCodeVerifier = codeVerifier

        // 2. Build the MAL Authorization URL
        // Note: MAL supports 'plain' code_challenge_method, making the challenge and verifier identical
        return "https://myanimelist.net/v1/oauth2/authorize" +
                "?response_type=code" +
                "&client_id=$clientId" +
                "&code_challenge=$codeVerifier" +
                "&code_challenge_method=plain"
    }

    override suspend fun getAccessToken(): String? {
        // Preserving your debug logic
        if (BuildConfig.IS_DEBUG) {
            // Note: Make sure you have a MAL_TOKEN in your BuildConfig if you use this
            return BuildConfig.MAL_TOKEN// Or BuildConfig.MAL_TOKEN
        }

        val state = authState.value
        return if (state is AuthState.Authenticated) state.accessToken else null
    }

    override suspend fun handleAuthorizationCode(code: String): Result<Unit> {
        return try {
            val verifier = currentCodeVerifier
                ?: throw IllegalStateException("Code verifier is missing. Auth flow broken.")

            // Here you will call your MalApi to exchange the code for the token
            // The request will need:
            // - client_id
            // - code
            // - code_verifier = verifier
            // - grant_type = "authorization_code"

            // Example:
            // val response = malApi.exchangeCodeForToken(clientId, code, verifier)
            // val token = response.accessToken

            // For now, simulating success
            val token = "mock_mal_token"
            _authState.value = AuthState.Authenticated(token)

            // Clear the verifier from memory now that we are done with it
            currentCodeVerifier = null

            // TODO: Save token (and refresh_token) to secure local storage
            Result.success(Unit)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "MAL Auth Failed")

            // Cleanup on failure
            currentCodeVerifier = null
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        _authState.value = AuthState.Unauthenticated
        currentCodeVerifier = null
        // TODO: Clear token from secure local storage
    }

    /**
     * Helper function to generate a PKCE compliant code verifier.
     * MAL requires a string between 43 and 128 characters containing [a-zA-Z0-9-._~].
     */
    private fun generateCodeVerifier(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        val length = 128 // Max length for extra security
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}