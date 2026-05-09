package com.ghost.zeku.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghost.zeku.data.repository.auth.AuthRedirectListener
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Auth process specifically for the transition period.
 * The persistent state (LoggedIn/LoggedOut) is managed by the AuthRepository's StateFlow.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastLoginResult: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val redirectListener: AuthRedirectListener
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Orchestrates the entire login flow.
     * @param provider AniList or MAL
     * @param openBrowser A lambda from the UI layer to launch the system browser
     */
    fun login(provider: ProviderType, openBrowser: (String) -> Unit) {
        viewModelScope.launch {
            // Update UI to show we are initiating the flow
            _uiState.update { it.copy(isLoading = true, error = null, lastLoginResult = null) }

            try {
                // 1. Get the Platform-Specific URL
                val authUrl = authRepository.getAuthorizationUrl(provider)

                // 2. Open the browser
                openBrowser(authUrl)

                // 3. SUSPEND and wait for the redirect
                // On Desktop: Starts loopback server. On Android: Awaits Activity Intent.
                Napier.d { "Awaiting redirect for ${provider.name}..." }
                val redirectUri = redirectListener.waitForRedirect()

                if (redirectUri != null) {
                    // 4. Extract tokens and complete the login
                    when (val result = authRepository.handleAuthRedirectUri(provider, redirectUri)) {
                        is ApiResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    lastLoginResult = "Successfully logged into ${provider.name}"
                                )
                            }
                        }

                        is ApiResult.Error -> {
                            // Note: The AuthRepository will also update the provider's
                            // AuthState to AuthState.Error(message) internally.
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.error.message
                                )
                            }
                        }

                        is ApiResult.Empty -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Received empty response from provider."
                                )
                            }
                        }

                        else -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Login timed out or was cancelled by the user."
                        )
                    }
                }

            } catch (e: Exception) {
                Napier.e(e) { "Auth flow encountered a critical failure" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred during login."
                    )
                }
            }
        }
    }

    /**
     * Clears credentials for the specific provider.
     */
    fun logout(provider: ProviderType) {
        viewModelScope.launch {
            authRepository.logout(provider)
            _uiState.update { it.copy(lastLoginResult = "Logged out of ${provider.name}") }
        }
    }

    /**
     * Resets the local error state (e.g. after showing a Snackbar).
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}