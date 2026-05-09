package com.ghost.zeku.presentation.viewmodel.main


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghost.zeku.domain.AuthSessionHandler
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.api.ApiResult
import com.ghost.zeku.domain.model.api.getErrorMessage
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.domain.repository.UserRepository
import com.ghost.zeku.utils.BrowserLauncher
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI


// ========================================================================
// VIEWMODEL
// ========================================================================

class MainViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val authHandler: AuthSessionHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(MainContract.State())
    val state: StateFlow<MainContract.State> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MainContract.Effect>()
    val effects: SharedFlow<MainContract.Effect> = _effects.asSharedFlow()

    init {
        // Automatically start observing the global repositories as soon as the app launches
        onEvent(MainContract.Event.Initialize)
    }

    fun onEvent(event: MainContract.Event) {
        when (event) {
            is MainContract.Event.Initialize -> observeGlobalState()

            is MainContract.Event.SwitchAccount -> {
                viewModelScope.launch {
                    Napier.d { "User requested account switch to: ${event.user.source}" }
                    userRepository.switchProvider(event.user.source)
                }
            }

            is MainContract.Event.Logout -> {
                logoutAccount(event.user)
            }

            is MainContract.Event.AddAccountClick -> {
                login(event.provider)
            }

            MainContract.Event.OpenZekuSite -> {
                Napier.d { "Open Zeku Site requested" }
            }

            is MainContract.Event.ViewAccount -> {
                Napier.d { "User requested account view from: ${event.user}" }
            }
        }
    }

    private fun login(provider: ProviderType) {
        viewModelScope.launch {
            Napier.d { "Starting login flow for ${provider.name}..." }

            // 1. Get the URL to open the provider's login page
            val authUrl = authRepository.getAuthorizationUrl(provider)

            if (authUrl == null) {
                sendEffect(MainContract.Effect.ShowMessage("Failed to generate login URL", MessageType.Error.Network))
                return@launch
            }

            // 2. Determine the path to listen for based on the provider
            val listenPath = provider.authPath

            Napier.d { "Awaiting browser redirect on path: $listenPath..." }

            // 3. Open browser and suspend until we get the authorization code
            val authCode = authHandler.getAuthorizationCode(authUrl, listenPath)

            BrowserLauncher.openUrl(authUrl)

            if (authCode != null) {
                Napier.d { "Code received! Exchanging for tokens..." }

                // 4. Extract tokens and complete the login using the RETURNED code
                when (val result = authRepository.handleAuthRedirectUri(provider, authCode)) {

                    is ApiResult.Success -> {
                        Napier.d { "Login successful!" }
                        // Show success toast/snackbar
                        sendEffect(
                            MainContract.Effect.ShowMessage(
                                "Successfully logged into ${provider.name}!",
                                MessageType.Success
                            )
                        )

                        // Refresh the user state so the UI updates
                        onEvent(MainContract.Event.Initialize)
                    }

                    is ApiResult.Error -> {
                        Napier.e { "Login failed: ${result.getErrorMessage()}" }
                        // Show error toast/snackbar
                        sendEffect(
                            MainContract.Effect.ShowMessage(
                                result.getErrorMessage() ?: "Login Failed",
                                MessageType.Error.Network
                            )
                        )
                        // State update handled internally by AuthRepository
                    }

                    is ApiResult.Empty -> {
                        sendEffect(
                            MainContract.Effect.ShowMessage(
                                "Unexpected empty response from server.",
                                MessageType.Error.Network
                            )
                        )
                    }

                    else -> {
                        Napier.w { "Unhandled ApiResult type." }
                    }
                }
            } else {
                // This happens if the user closes the browser tab without logging in
                // or if the local Ktor server times out/cancels.
                Napier.i { "Login cancelled by user." }
                sendEffect(MainContract.Effect.ShowMessage("Login cancelled.", MessageType.Info))
            }
        }
    }

    /**
     * Combines the highly optimized StateFlows from UserRepository into a single UI State.
     */
    private fun observeGlobalState() {
        viewModelScope.launch {
            combine(
                userRepository.currentUser,
                userRepository.allUsers,
                userRepository.activeProvider
            ) { activeUser, allUsers, providerStr ->

                // Safely parse the active provider
                val parsedProvider = providerStr ?: ProviderType.ANILIST

                MainContract.State(
                    currentUser = activeUser,
                    availableUsers = allUsers,
                    activeProvider = parsedProvider
                )
            }.collectLatest { combinedState ->
                _state.value = combinedState
            }
        }
    }

    private fun logoutAccount(user: UserProfile) {
        val providerType = user.source
        viewModelScope.launch {
            _state.update { it.copy(isLoggingOut = true) }
            try {
                // 1. Tell the central Auth repo to wipe the tokens
                authRepository.logout(providerType)

                // Note: We don't need to manually clear the UserRepository!
                // Because UserRepository is listening to AuthRepository, it will automatically
                // delete the Room database row and update our state.

                sendEffect(MainContract.Effect.ShowMessage("Logged out of ${providerType.name}", MessageType.Success))

                // If they logged out of their last account, maybe require login
                if (_state.value.availableUsers.size <= 1) {
                    sendEffect(MainContract.Effect.RequireLogin)
                }

            } catch (e: Exception) {
                Napier.e(e) { "Failed to logout of ${providerType.name}" }
                sendEffect(MainContract.Effect.ShowMessage("Logout failed", MessageType.Error.Unknown))
            } finally {
                _state.update { it.copy(isLoggingOut = false) }
            }
        }
    }

    private fun sendEffect(effect: MainContract.Effect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}