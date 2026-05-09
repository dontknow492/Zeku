package com.ghost.zeku.data.repository

import com.ghost.zeku.data.local.room.dao.UserDao
import com.ghost.zeku.data.local.room.toDomain
import com.ghost.zeku.data.local.room.toEntity
import com.ghost.zeku.domain.MediaSource
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.model.api.getErrorMessage
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.domain.repository.UserRepository
import com.ghost.zeku.domain.repository.UserSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class UserRepositoryImpl(
    private val userSettings: UserSettings,
    private val authRepository: AuthRepository, // INJECT YOUR CENTRALIZED REPO HERE!
    private val userDao: UserDao,
    private val mediaSources: List<MediaSource> // INJECT YOUR SOURCES INSTEAD!
) : UserRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // The currently selected provider (Ideally backed by DataStore/Preferences!)

    override val activeProvider = userSettings.preferences.map { it.activeProvider }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Lazily,
        initialValue = userSettings.preferences.value.activeProvider
    )

    // 1. ACTIVE USER: Automatically switches when _activeProvider changes
    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: StateFlow<UserProfile?> = activeProvider
        .flatMapLatest { provider ->
            userDao.getUserProfileFlow(provider).map { it?.toDomain() }
        }
        .stateIn(repositoryScope, SharingStarted.Eagerly, null)

    // 2. ALL USERS: For your account switcher UI
    override val allUsers: StateFlow<List<UserProfile>> = userDao.getAllUserProfilesFlow()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    init {
        // Listen to AuthState for EVERY provider cleanly using the Map!
        ProviderType.entries.forEach { provider ->
            repositoryScope.launch {
                authRepository.authStates
                    // Extract just this specific provider's state from the map
                    .map { it[provider] ?: AuthState.LoggedOut }
                    // CRUCIAL: Only trigger the collect block if THIS provider's state actually changed!
                    // Without this, logging into MAL would trigger an AniList fetch too.
                    .distinctUntilChanged()
                    .collectLatest { state ->
                        when (state) {
                            is AuthState.LoggedIn -> {
                                Napier.d { "Logged into ${provider}. Fetching profile..." }
                                val result = fetchUserProfile(provider)
                                Napier.d { "Fetched ${provider}: $result" }
                            }

                            is AuthState.LoggedOut, is AuthState.Error -> {
                                Napier.d { "Auth ended for ${provider}. Clearing local data." }
                                clearUserData(provider)
                            }

                            else -> { /* Ignore loading state */
                            }
                        }
                    }
            }
        }
    }

    override suspend fun switchProvider(provider: ProviderType) {

        // Verify it's a valid enum string before switching
        userSettings.updatePreferences { it.copy(activeProvider = provider) }
        Napier.d { "Switched active app provider to: $provider" }
    }

    override suspend fun fetchUserProfile(provider: ProviderType?): Result<Unit> {
        val targetProvider =
            provider ?: activeProvider.value ?: return Result.failure(Exception("No provider selected"))

        val source = mediaSources.find { it.getProviderType() == targetProvider }
            ?: return Result.failure(Exception("No MediaSource implemented for $targetProvider"))

        return try {
            val token = authRepository.getAccessToken(targetProvider)
                ?: throw IllegalStateException("Cannot fetch profile without a token")

            // FIX: Pass the token to the source!
            val actualUser = when (val result = source.getCurrentUser()) {
                is ApiResult.Empty -> throw IllegalStateException("No user found")
                is ApiResult.Error -> throw result.error.cause ?: Exception(result.getErrorMessage())
                is ApiResult.Loading -> throw Exception("Fetching profile")
                is ApiResult.Success<UserProfile> -> result.data // Assuming ApiResult.Success holds the data
            }

            userDao.insertUser(actualUser.toEntity())
            Napier.d { "Successfully fetched and cached profile for: $targetProvider" }
            Result.success(Unit)

        } catch (e: Exception) {
            Napier.e(e) { "Failed to fetch user profile for $targetProvider" }
            Result.failure(e)
        }
    }

    override suspend fun clearUserData(provider: ProviderType?) {
        Napier.d { "Request to clear user data" }
        if (provider == null) {
            Napier.d { "No user found" }
            return
        }

        Napier.d { "Clearing user data for user: $provider" }

        val targetProvider = provider ?: activeProvider.value ?: return
        userDao.deleteUser(targetProvider)

        // If we deleted the active user, fallback to null (or handle fallback logic)
        if (activeProvider.value == targetProvider) {
            userSettings.updatePreferences { it.copy(activeProvider = targetProvider) }
        }
    }
}