package com.ghost.zeku.presentation.viewmodel.main


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.domain.repository.UserRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


// ========================================================================
// VIEWMODEL
// ========================================================================

class MainViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
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

            MainContract.Event.AddAccountClick -> {
                Napier.d { "Add account click" }
            }

            MainContract.Event.OpenZekuSite -> {
                Napier.d { "Open Zeku Site requested" }
            }

            is MainContract.Event.ViewAccount -> {
                Napier.d { "User requested account view from: ${event.user}" }
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