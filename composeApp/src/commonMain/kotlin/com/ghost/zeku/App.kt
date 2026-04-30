package com.ghost.zeku

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.ghost.zeku.domain.model.enum.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.presentation.theme.AppTheme
import org.koin.compose.koinInject


@Composable
fun App() = AppTheme {
//    val userRepository: UserRepository = koinInject()

    val authRepository: AuthRepository = koinInject()
//
    var token by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        token = authRepository.getAccessToken(ProviderType.ANILIST)
    }
//
    SelectionContainer {
        Text(token ?: "No token provided")
    }


//    val users by userRepository.allUsers.collectAsState(initial = listOf())
//    Column {
//        users.forEach {
//            UserProfileItem(
//                user = it,
//                onAvatarClick = {},
//                onLogout = {},
//                isExpanded = true
//            )
//        }
//    }
//    val userSettings: UserSettings = koinInject()
//    userSettings.updatePreferences {
//        it.copy(
//            activeProvider = ProviderType.MYANIMELIST,
//            homeTimeout = TimeUnit.DAYS.toMillis(30) // TODO: fix in production
//        )
//    }
//    ZekuAppWrapper()
}




