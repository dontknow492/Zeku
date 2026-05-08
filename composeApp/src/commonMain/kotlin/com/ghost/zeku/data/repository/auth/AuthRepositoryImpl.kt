package com.ghost.zeku.data.repository.auth

import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.model.api.ErrorType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.domain.repository.ProviderAuthRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * The Central Coordinator.
 * It iterates through a list of individual Provider Repositories (AniList, MAL)
 * and groups their logic so the UI only has to talk to one class.
 */
class AuthRepositoryImpl(
    private val providers: List<ProviderAuthRepository>
) : AuthRepository {

    // A map to quickly look up the right repository for the requested ProviderType
    private val providerMap: Map<ProviderType, ProviderAuthRepository> =
        providers.associateBy { it.providerType }

    init {
        Napier.i { "AuthRepository initialized with providers: ${providerMap.keys}" }
    }

    // Dynamically combines the StateFlows of all individual providers into one massive Map!
    override val authStates: StateFlow<Map<ProviderType, AuthState>> = combine(
        providers.map { repo ->
            flow {
                Napier.d { "Starting auth state collection for ${repo.providerType}" }
                repo.authState.collect { state ->
                    Napier.v { "Auth state updated for ${repo.providerType}: $state" }
                    emit(repo.providerType to state)
                }
            }
        }
    ) { pairs ->
        pairs.toMap().also { combinedStates ->
            // Log significant auth state changes at INFO level
            combinedStates.forEach { (type, state) ->
                if (state is AuthState.LoggedIn) {
                    Napier.i { "Provider $type is authenticated" }
                } else if (state is AuthState.Error) {
                    Napier.w { "Provider $type auth error: ${state.message}" }
                }
            }
        }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = providers.associate { it.providerType to it.authState.value }
    )

    private fun getRepoOrThrow(provider: ProviderType): ProviderAuthRepository {
        return providerMap[provider] ?: run {
            Napier.e { "Authentication not supported for provider: $provider" }
            throw IllegalArgumentException("Authentication not supported for $provider")
        }
    }

    override fun isUserLoggedIn(provider: ProviderType): Boolean {
        val loggedIn = providerMap[provider]?.isUserLoggedIn() ?: false
        Napier.v { "Login check for $provider: $loggedIn" }
        return loggedIn
    }

    override fun getUserId(provider: ProviderType): Int? {
        val loggedIn = providerMap[provider]?.isUserLoggedIn() ?: false
        if (!loggedIn) return 0
        val id = providerMap[provider]?.getUserId()
        Napier.v { "User ID for $provider: $id" }
        return id
    }

    override fun getAuthorizationUrl(provider: ProviderType): String {
        Napier.d { "Generating authorization URL for $provider" }
        val url = getRepoOrThrow(provider).getAuthorizationUrl()
        Napier.v { "Authorization URL generated for $provider: ${url.take(50)}..." }
        return url
    }

    override suspend fun handleAuthRedirectUri(provider: ProviderType, uriString: String): ApiResult<Unit> {
        Napier.d { "Handling auth redirect for $provider" }

        val repo = providerMap[provider]
        if (repo == null) {
            val message = "Authentication not supported for provider: $provider"
            Napier.e { message }
            return ApiResult.Error(
                error = ApiError(
                    type = ErrorType.BAD_REQUEST,
                    message = message,
                    recoverable = false
                )
            )
        }

        val result = repo.handleAuthRedirectUri(uriString)
        when (result) {
            is ApiResult.Success -> {
                Napier.i { "Auth redirect handled successfully for $provider" }
            }

            is ApiResult.Error -> {
                Napier.e(result.error.cause) { "Auth redirect failed for $provider: ${result.error.message}" }
            }

            else -> {}
        }
        return result
    }

    override suspend fun logout(provider: ProviderType) {
        Napier.d { "Logging out from $provider" }
        providerMap[provider]?.logout()
        Napier.i { "Logged out from $provider" }
    }

    override suspend fun getAccessToken(provider: ProviderType): String? {
        val token = providerMap[provider]?.getAccessToken()
        if (token != null) {
            Napier.v { "Access token retrieved for $provider: ${token.take(4)}****" }
        } else {
            Napier.w { "No access token available for $provider" }
        }
        return token
    }
}