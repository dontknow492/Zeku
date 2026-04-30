package com.ghost.zeku.presentation.viewmodel.main

import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.enum.ProviderType

// ========================================================================
// 1. CONTRACT (State, Events, Effects)
// ========================================================================

interface MainContract {
    data class State(
        val currentUser: UserProfile? = null,
        val availableUsers: List<UserProfile> = emptyList(),
        val activeProvider: ProviderType = ProviderType.MYANIMELIST,
        val isLoggingOut: Boolean = false
    )

    sealed interface Event {
        data object Initialize : Event
        data class SwitchAccount(val providerType: ProviderType) : Event
        data class Logout(val providerType: ProviderType) : Event
    }

    sealed interface Effect {
        data class ShowMessage(val message: String, val type: MessageType) : Effect

        // Used to globally kick the user to the login screen if needed
        data object RequireLogin : Effect
    }
}