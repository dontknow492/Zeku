package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import org.ghost.zeku.R
import org.ghost.zeku.ui.common.SettingScaffold
import org.ghost.zeku.ui.component.GroupSettingItem
import org.ghost.zeku.ui.component.InputSettingItem
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.screen.settings.NetworkSettingsState

sealed interface NetworkSettingsEvent {
    data class OnConcurrentChange(val concurrent: String) : NetworkSettingsEvent
    data class OnCellularDownloadChange(val cellularDownload: Boolean) : NetworkSettingsEvent
    data class OnAria2cChange(val aria2c: Boolean) : NetworkSettingsEvent
    data class OnProxyChange(val proxy: Boolean) : NetworkSettingsEvent
    data class OnProxyUrlChange(val proxyUrl: String) : NetworkSettingsEvent
    data class OnCookiesChange(val cookies: String) : NetworkSettingsEvent
    data class OnUserAgentChange(val userAgent: String) : NetworkSettingsEvent
    data class OnUserAgentStringChange(val userAgentString: String) : NetworkSettingsEvent
    data class OnRateLimitChange(val rateLimit: Boolean) : NetworkSettingsEvent
    data class OnMaxRateChange(val maxRate: String) : NetworkSettingsEvent
    data class OnRetriesChange(val retries: String) : NetworkSettingsEvent
    data class OnFragmentRetriesChange(val fragmentRetries: String) : NetworkSettingsEvent
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
    SettingScaffold(
        modifier = modifier,
        title = stringResource(R.string.settings_network_title),
        onBackClick = onBackClick
    ) {
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
            InputSettingItem(
                value = state.maxRate.toString(),
                onValueChange = { value ->
                    eventHandler.invoke(
                        NetworkSettingsEvent.OnMaxRateChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_max_download_rate),
                label = stringResource(R.string.max_rate_label),
                placeholder = stringResource(R.string.max_rate_placeholder),
                // Descriptive message + Value
                description = stringResource(
                    R.string.desc_max_download_rate_full,
                    state.maxRate.toString()
                ),
                icon = ImageVector.vectorResource(R.drawable.rounded_speed_24),
                enabled = state.rateLimit,
                isError = !state.isValidMaxRate,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            InputSettingItem(
                value = state.retries.toString(),
                onValueChange = { value ->
                    eventHandler.invoke(
                        NetworkSettingsEvent.OnRetriesChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_connection_retries),
                // Descriptive message + Value
                description = stringResource(
                    R.string.desc_connection_retries_full,
                    state.retries.toString()
                ),
                label = stringResource(R.string.connection_retries_label),
                placeholder = stringResource(R.string.connection_retries_placeholder),
                icon = ImageVector.vectorResource(R.drawable.rounded_sync_24),
                isError = !state.isValidRetries,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            InputSettingItem(
                value = state.fragmentRetries.toString(),
                onValueChange = { value ->
                    eventHandler.invoke(
                        NetworkSettingsEvent.OnFragmentRetriesChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_fragment_retries),
                // Descriptive message + Value
                description = stringResource(
                    R.string.desc_fragment_retries_full,
                    state.fragmentRetries.toString()
                ),
                label = stringResource(R.string.fragment_retries_label),
                placeholder = stringResource(R.string.fragment_retries_placeholder),
                icon = Icons.Filled.Settings,
                isError = !state.isValidFragmentRetries,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        }

        // Download Configuration Group
        GroupSettingItem(
            title = stringResource(R.string.group_title_download_configuration)
        ) {
            InputSettingItem(
                value = state.concurrent.toString(),
                onValueChange = { value ->
                    eventHandler.invoke(
                        NetworkSettingsEvent.OnConcurrentChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_concurrent_downloads),
                // Descriptive message + Value
                description = stringResource(
                    R.string.desc_concurrent_downloads_full,
                    state.concurrent.toString()
                ),
                label = stringResource(R.string.concurrent_label),
                placeholder = stringResource(R.string.concurrent_placeholder),
                icon = Icons.Filled.Settings,
                isError = !state.isValidConcurrent,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
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
            InputSettingItem(
                value = state.proxyUrl,
                onValueChange = { value ->
                    eventHandler.invoke(
                        NetworkSettingsEvent.OnProxyUrlChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_proxy_server_url),
                // Descriptive message + Value
                description = stringResource(
                    R.string.desc_proxy_server_url_full,
                    state.proxyUrl
                ),
                label = stringResource(R.string.proxy_url_label),
                placeholder = stringResource(R.string.proxy_url_placeholder),
                icon = ImageVector.vectorResource(R.drawable.rounded_link_24),
                isError = !state.isValidProxyUrl
            )

            InputSettingItem(
                value = state.cookies,
                onValueChange = { value ->
                    eventHandler.invoke(
                        NetworkSettingsEvent.OnCookiesChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_authentication_cookies),
                // Descriptive message + Value
                description = stringResource(
                    R.string.desc_authentication_cookies_full,
                    state.cookies
                ),
                label = stringResource(R.string.cookies_label),
                placeholder = stringResource(R.string.cookies_placeholder),
                icon = ImageVector.vectorResource(R.drawable.baseline_cookie_24),
                isError = !state.isValidCookies
            )
        }

        // User Agent Group
        GroupSettingItem(
            title = stringResource(R.string.group_title_client_identification)
        ) {
            InputSettingItem(
                value = state.userAgent,
                onValueChange = { value ->
                    eventHandler.invoke(
                        NetworkSettingsEvent.OnUserAgentChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_default_user_agent),
                // Descriptive message + Value
                description = stringResource(
                    R.string.desc_default_user_agent_full,
                    state.userAgent
                ),
                label = stringResource(R.string.user_agent_label),
                placeholder = stringResource(R.string.user_agent_placeholder),
                icon = ImageVector.vectorResource(R.drawable.rounded_support_agent_24),
                isError = !state.isValidUserAgent
            )

            InputSettingItem(
                value = state.userAgentString,
                onValueChange = { value ->
                    eventHandler.invoke(
                        NetworkSettingsEvent.OnUserAgentStringChange(
                            value
                        )
                    )
                },
                title = stringResource(R.string.title_custom_user_agent_string),
                // Descriptive message + Value
                description = stringResource(
                    R.string.desc_custom_user_agent_string_full,
                    state.userAgentString
                ),
                label = stringResource(R.string.user_agent_string_label),
                placeholder = stringResource(R.string.user_agent_string_placeholder),
                icon = ImageVector.vectorResource(R.drawable.rounded_abc_24),
                isError = !state.isValidUserAgentString
            )
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