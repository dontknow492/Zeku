package com.ghost.zeku

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.domain.repository.UserRepository
import com.ghost.zeku.domain.repository.UserSettings
import com.ghost.zeku.presentation.components.item.UserProfileItem
import com.ghost.zeku.presentation.navigation.ZekuAppWrapper
import com.ghost.zeku.presentation.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.dsl.koinApplication
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit


@Composable
fun App() = AppTheme {
    val userRepository: UserRepository = koinInject()

//    val authRepository: AuthRepository = koinInject()
//
//    var token by remember { mutableStateOf<String?>(null) }
//    LaunchedEffect(Unit) {
//        token = authRepository.getAccessToken(ProviderType.MYANIMELIST)
//    }
//
//    SelectionContainer {
//        Text(token ?: "No token provided")
//    }


    val users by userRepository.allUsers.collectAsState(initial = listOf())
    Column {
        users.forEach {
            UserProfileItem(
                user = it,
                onAvatarClick = {},
                onLogout = {},
                isExpanded = true
            )
        }
    }
//    val userSettings: UserSettings = koinInject()
//    userSettings.updatePreferences {
//        it.copy(
//            activeProvider = ProviderType.MYANIMELIST,
//            homeTimeout = TimeUnit.DAYS.toMillis(30) // TODO: fix in production
//        )
//    }
//    ZekuAppWrapper()
}




