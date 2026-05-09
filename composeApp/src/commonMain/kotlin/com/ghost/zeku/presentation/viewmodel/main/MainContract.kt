package com.ghost.zeku.presentation.viewmodel.main

import com.ghost.zeku.domain.model.MessageType
import com.ghost.zeku.domain.model.UserProfile
import com.ghost.zeku.domain.model.ProviderType

// ========================================================================
// 1. CONTRACT (State, Events, Effects)
// ========================================================================

interface MainContract {
    data class State(
        val currentUser: UserProfile? = null,
        val availableUsers: List<UserProfile> = emptyList(),
        val activeProvider: ProviderType = ProviderType.MYANIMELIST,
        val isLoggingOut: Boolean = false,
        val error: String? = null,
    )

    sealed interface Event {
        data object Initialize : Event
        data object OpenZekuSite : Event
        data class SwitchAccount(val user: UserProfile) : Event
        data class Logout(val user: UserProfile) : Event
        data class AddAccountClick(val provider: ProviderType) : Event
        data class ViewAccount(val user: UserProfile) : Event
    }

    sealed interface Effect {
        data class ShowMessage(val message: String, val type: MessageType) : Effect

        // Used to globally kick the user to the login screen if needed
        data object RequireLogin : Effect
    }
}