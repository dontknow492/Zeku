package com.ghost.zeku.domain.repository

import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.model.enum.ProviderType
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {

    /**
     * Emits the current authentication state for ALL providers.
     * Your Settings UI can observe this map to show "Logged In" next to AniList
     * and "Login" next to MyAnimeList at the same time.
     */
    val authStates: StateFlow<Map<ProviderType, AuthState>>

    /**
     * Synchronous check to see if a specific provider is logged in.
     * Useful for quick UI checks (e.g., hiding the "Add to List" button).
     */
    fun isUserLoggedIn(provider: ProviderType): Boolean

    /**
     * Return the userId of current user
     */
    fun getUserId(provider: ProviderType): Int?

    /**
     * 1. Generate the URL to open in the external browser.
     * The repository handles creating the MAL PKCE challenges or AniList Client IDs under the hood.
     */
    fun getAuthorizationUrl(provider: ProviderType): String

    /**
     * 2. The Magic Handler.
     * Instead of passing just a 'code', pass the ENTIRE redirect URI that the browser sends back.
     * Why? Because MAL puts a ?code= in the URI, but AniList puts an #access_token= in the URI.
     * Passing the full string lets the Repository parse it perfectly without the UI caring!
     */
    suspend fun handleAuthRedirectUri(provider: ProviderType, uriString: String): ApiResult<Unit>

    /**
     * 3. Disconnect an account and wipe its tokens from secure storage.
     */
    suspend fun logout(provider: ProviderType)

    /**
     * 4. Retrieve the token.
     * This is a suspend function because for MAL, it might need to silently hit the network
     * to exchange a Refresh Token for a new Access Token before returning!
     */
    suspend fun getAccessToken(provider: ProviderType): String?


}



