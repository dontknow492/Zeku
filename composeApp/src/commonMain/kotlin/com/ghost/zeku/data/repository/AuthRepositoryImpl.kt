package com.ghost.zeku.data.repository

import com.ghost.zeku.domain.model.api.ApiError
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.model.api.ErrorType
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.domain.repository.ProviderAuthRepository
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

    // Dynamically combines the StateFlows of all individual providers into one massive Map!
    override val authStates: StateFlow<Map<ProviderType, AuthState>> = combine(
        providers.map { repo ->
            // Create a flow that emits Pair<ProviderType, AuthState>
            flow {
                repo.authState.collect { state -> emit(repo.providerType to state) }
            }
        }
    ) { pairs ->
        pairs.toMap()
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = providers.associate { it.providerType to it.authState.value }
    )

    private fun getRepoOrThrow(provider: ProviderType): ProviderAuthRepository {
        return providerMap[provider] ?: throw IllegalArgumentException("Authentication not supported for $provider")
    }

    override fun isUserLoggedIn(provider: ProviderType): Boolean {
        return providerMap[provider]?.isUserLoggedIn() ?: false
    }

    override fun getAuthorizationUrl(provider: ProviderType): String {
        return getRepoOrThrow(provider).getAuthorizationUrl()
    }

    override suspend fun handleAuthRedirectUri(provider: ProviderType, uriString: String): ApiResult<Unit> {
        val repo = providerMap[provider] ?: return ApiResult.Error(
            error = ApiError(
                type = ErrorType.BAD_REQUEST,
                message = "Authentication not supported for provider: $provider",
                recoverable = false
            )
        )
        return repo.handleAuthRedirectUri(uriString)
    }

    override suspend fun logout(provider: ProviderType) {
        providerMap[provider]?.logout()
    }

    override suspend fun getAccessToken(provider: ProviderType): String? {
        return providerMap[provider]?.getAccessToken()
    }
}