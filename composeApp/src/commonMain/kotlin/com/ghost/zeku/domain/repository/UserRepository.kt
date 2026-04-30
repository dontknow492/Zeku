package com.ghost.zeku.domain.repository

import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.enum.ProviderType
import kotlinx.coroutines.flow.StateFlow


interface UserRepository {
    val currentUser: StateFlow<UserProfile?>
    val allUsers: StateFlow<List<UserProfile>> // For the "Switch Accounts" UI
    val activeProvider: StateFlow<ProviderType?> // Knows which provider is currently driving the app

    suspend fun switchProvider(provider: ProviderType)
    suspend fun fetchUserProfile(provider: ProviderType? = null): Result<Unit>
    suspend fun clearUserData(provider: ProviderType? = null)
}