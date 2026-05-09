package com.ghost.zeku.presentation.temp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.api.AuthState
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.repository.AuthRepository
import com.ghost.zeku.presentation.auth.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun TestAuthScreen(
    viewModel: AuthViewModel
) {
    // We also inject the repo directly just for the "Verify Storage" debug button
    val authRepository: AuthRepository = koinInject()

    val uiState by viewModel.uiState.collectAsState()
    val authStates by authRepository.authStates.collectAsState()

    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var logMessage by remember { mutableStateOf("Awaiting user action...") }

    // Sync ViewModel errors to our log console
    LaunchedEffect(uiState.error) {
        uiState.error?.let { logMessage = "Error: $it" }
    }

    LaunchedEffect(uiState.lastLoginResult) {
        uiState.lastLoginResult?.let { logMessage = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Auth Testing Ground", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Check redirects & secure storage",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- ANILIST CARD ---
        ProviderAuthCard(
            title = "AniList",
            provider = ProviderType.ANILIST,
            state = authStates[ProviderType.ANILIST] ?: AuthState.LoggedOut,
            isLoading = uiState.isLoading,
            onLoginClick = {
                logMessage = "Starting AniList Flow..."
                viewModel.login(ProviderType.ANILIST) { url ->
                    uriHandler.openUri(url)
                }
            },
            onLogoutClick = {
                viewModel.logout(ProviderType.ANILIST)
                logMessage = "AniList logged out."
            },
            onCheckToken = {
                scope.launch {
                    val token = authRepository.getAccessToken(ProviderType.ANILIST)
                    logMessage = if (token != null) {
                        "AniList Token Found!\n${token.take(15)}... (Len: ${token.length})"
                    } else {
                        "No AniList token in storage."
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- MYANIMELIST CARD ---
        ProviderAuthCard(
            title = "MyAnimeList (PKCE)",
            provider = ProviderType.MYANIMELIST,
            state = authStates[ProviderType.MYANIMELIST] ?: AuthState.LoggedOut,
            isLoading = uiState.isLoading,
            onLoginClick = {
                logMessage = "Starting MYANIMELIST Flow (Generating Verifier)..."
                viewModel.login(ProviderType.MYANIMELIST) { url ->
                    uriHandler.openUri(url)
                }
            },
            onLogoutClick = {
                viewModel.logout(ProviderType.MYANIMELIST)
                logMessage = "MYANIMELIST logged out."
            },
            onCheckToken = {
                scope.launch {
                    val token = authRepository.getAccessToken(ProviderType.MYANIMELIST)
                    logMessage = if (token != null) {
                        "MYANIMELIST Token Found!\n${token.take(15)}... (Len: ${token.length})"
                    } else {
                        "No MYANIMELIST token in storage."
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- LOG CONSOLE ---
        Text(
            text = "Activity Log",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = logMessage,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProviderAuthCard(
    title: String,
    provider: ProviderType,
    state: AuthState,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onCheckToken: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (state == AuthState.LoggedIn)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)

                if (state == AuthState.Loading || (isLoading && state != AuthState.LoggedIn)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Badge(
                        containerColor = when (state) {
                            AuthState.LoggedIn -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ) {
                        Text(state.toString(), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state == AuthState.LoggedIn) {
                    Button(
                        onClick = onLogoutClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Logout")
                    }
                } else {
                    Button(onClick = onLoginClick, enabled = !isLoading) {
                        Text("Login")
                    }
                }

                OutlinedButton(onClick = onCheckToken) {
                    Text("Verify Storage")
                }
            }
        }
    }
}