package com.ghost.zeku.domain.repository

import com.ghost.zeku.domain.model.api.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    // Current state of the user (Logged in or not)
    val authState: StateFlow<AuthState>

    // 1. Generate the URL to open in the browser
    fun getAuthorizationUrl(): String

    // 2. Exchange the 'code' from the redirect for a real token
    suspend fun handleAuthorizationCode(code: String): Result<Unit>

    // 3. Simple logout
    suspend fun logout()

    // 4. Access Token
    suspend fun getAccessToken(): String?
}