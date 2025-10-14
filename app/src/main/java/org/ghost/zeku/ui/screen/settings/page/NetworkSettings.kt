package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R
import org.ghost.zeku.ui.component.GroupSettingItem
import org.ghost.zeku.ui.component.SettingItem
import org.ghost.zeku.ui.component.SettingTitle
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.BackButton
import org.ghost.zeku.ui.screen.settings.NetworkSettingsState

sealed interface NetworkSettingsEvent {
    data class OnConcurrentChange(val concurrent: Int) : NetworkSettingsEvent
    data class OnCellularDownloadChange(val cellularDownload: Boolean) : NetworkSettingsEvent
    data class OnAria2cChange(val aria2c: Boolean) : NetworkSettingsEvent
    data class OnProxyChange(val proxy: Boolean) : NetworkSettingsEvent
    data class OnProxyUrlChange(val proxyUrl: String) : NetworkSettingsEvent
    data class OnCookiesChange(val cookies: String) : NetworkSettingsEvent
    data class OnUserAgentChange(val userAgent: String) : NetworkSettingsEvent
    data class OnUserAgentStringChange(val userAgentString: String) : NetworkSettingsEvent
    data class OnRateLimitChange(val rateLimit: Boolean) : NetworkSettingsEvent
    data class OnMaxRateChange(val maxRate: Int) : NetworkSettingsEvent
    data class OnRetriesChange(val retries: Int) : NetworkSettingsEvent
    data class OnFragmentRetriesChange(val fragmentRetries: Int) : NetworkSettingsEvent
    data class OnForceIpv4Change(val forceIpv4: Boolean) : NetworkSettingsEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettings(
    modifier: Modifier = Modifier,
    state: NetworkSettingsState,
    eventHandler: (NetworkSettingsEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onBackClick = onBackClick) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Title
            SettingTitle(stringResource(R.string.settings_network_title))

            // Force IPv4 Switch
            SwitchSettingItem(
                title = stringResource(R.string.title_force_ipv4_connection),
                description = stringResource(R.string.desc_force_ipv4_connection),
                icon = ImageVector.vectorResource(R.drawable.rounded_filter_4_24),
                checked = state.forceIpv4,
                onSelectionChange = { checked ->
                    eventHandler(
                        NetworkSettingsEvent.OnForceIpv4Change(
                            checked
                        )
                    )
                }
            )

            // Rate/Limits Group
            GroupSettingItem(
                title = stringResource(R.string.group_title_connection_limits)
            ) {
                SwitchSettingItem(
                    title = stringResource(R.string.title_enable_rate_limiting),
                    description = stringResource(R.string.desc_enable_rate_limiting),
                    icon = ImageVector.vectorResource(R.drawable.rounded_delivery_truck_speed_24),
                    checked = state.rateLimit,
                    onSelectionChange = { checked ->
                        eventHandler(
                            NetworkSettingsEvent.OnRateLimitChange(
                                checked
                            )
                        )
                    }
                )
                SettingItem(
                    title = stringResource(R.string.title_max_download_rate),
                    // Descriptive message + Value
                    description = stringResource(
                        R.string.desc_max_download_rate_full,
                        state.maxRate.toString()
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_speed_24),
                    enabled = state.rateLimit,
                    onClick = { /* TODO: Handle Max Rate change */ }
                )
                SettingItem(
                    title = stringResource(R.string.title_connection_retries),
                    // Descriptive message + Value
                    description = stringResource(
                        R.string.desc_connection_retries_full,
                        state.retries.toString()
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_sync_24),
                    onClick = { /* TODO: Handle Retries change */ }
                )
                SettingItem(
                    title = stringResource(R.string.title_fragment_retries),
                    // Descriptive message + Value
                    description = stringResource(
                        R.string.desc_fragment_retries_full,
                        state.fragmentRetries.toString()
                    ),
                    icon = Icons.Filled.Settings,
                    onClick = { /* TODO: Handle Fragment Retries change */ }
                )
            }

            // Download Configuration Group
            GroupSettingItem(
                title = stringResource(R.string.group_title_download_configuration)
            ) {
                SettingItem(
                    title = stringResource(R.string.title_concurrent_downloads),
                    // Descriptive message + Value
                    description = stringResource(
                        R.string.desc_concurrent_downloads_full,
                        state.concurrent.toString()
                    ),
                    icon = Icons.Filled.Settings,
                    onClick = {}
                )
                SwitchSettingItem(
                    title = stringResource(R.string.title_allow_cellular_downloads),
                    description = stringResource(R.string.desc_allow_cellular_downloads),
                    icon = ImageVector.vectorResource(R.drawable.rounded_android_cell_4_bar_24),
                    checked = state.cellularDownload,
                    onSelectionChange = { download ->
                        eventHandler(
                            NetworkSettingsEvent.OnCellularDownloadChange(
                                download
                            )
                        )
                    }
                )
                SwitchSettingItem(
                    title = stringResource(R.string.title_use_aria2c_downloader),
                    description = stringResource(R.string.desc_use_aria2c_downloader),
                    icon = ImageVector.vectorResource(R.drawable.round_font_download_24),
                    checked = state.aria2c,
                    onSelectionChange = { checked ->
                        eventHandler(
                            NetworkSettingsEvent.OnAria2cChange(
                                checked
                            )
                        )
                    }
                )
            }

            // Proxy Settings Group
            GroupSettingItem(
                title = stringResource(R.string.group_title_proxy_settings)
            ) {
                SwitchSettingItem(
                    title = stringResource(R.string.title_enable_proxy),
                    description = stringResource(R.string.desc_enable_proxy),
                    icon = ImageVector.vectorResource(R.drawable.server),
                    checked = state.proxy,
                    onSelectionChange = { checked ->
                        eventHandler(
                            NetworkSettingsEvent.OnProxyChange(
                                checked
                            )
                        )
                    }
                )
                SettingItem(
                    title = stringResource(R.string.title_proxy_server_url),
                    // Descriptive message + Value
                    description = stringResource(
                        R.string.desc_proxy_server_url_full,
                        state.proxyUrl
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_link_24),
                    onClick = { /* TODO: Handle Proxy URL change */ }
                )
                SettingItem(
                    title = stringResource(R.string.title_authentication_cookies),
                    // Descriptive message + Value
                    description = stringResource(
                        R.string.desc_authentication_cookies_full,
                        state.cookies
                    ),
                    icon = ImageVector.vectorResource(R.drawable.baseline_cookie_24),
                    onClick = { /* TODO: Handle Cookies change */ }
                )
            }

            // User Agent Group
            GroupSettingItem(
                title = stringResource(R.string.group_title_client_identification)
            ) {
                SettingItem(
                    title = stringResource(R.string.title_default_user_agent),
                    // Descriptive message + Value
                    description = stringResource(
                        R.string.desc_default_user_agent_full,
                        state.userAgent
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_support_agent_24),
                    onClick = { /* TODO: Handle User Agent change */ }
                )
                SettingItem(
                    title = stringResource(R.string.title_custom_user_agent_string),
                    // Descriptive message + Value
                    description = stringResource(
                        R.string.desc_custom_user_agent_string_full,
                        state.userAgentString
                    ),
                    icon = ImageVector.vectorResource(R.drawable.rounded_abc_24),
                    onClick = { /* TODO: Handle User Agent String change */ }
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun NetworkSettingsPreview() {
    val state = NetworkSettingsState(
        concurrent = 4,
        cellularDownload = false,
        aria2c = false,
        proxy = false,
        proxyUrl = "",
        cookies = "",
        userAgent = "",
        userAgentString = "",
        rateLimit = false,
        maxRate = 0,
        retries = 3,
        fragmentRetries = 10,
        forceIpv4 = false
    )
    NetworkSettings(state = state, eventHandler = {}, onBackClick = {})
}