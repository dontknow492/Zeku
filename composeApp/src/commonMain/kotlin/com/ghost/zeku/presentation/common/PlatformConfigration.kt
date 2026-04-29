package com.ghost.zeku.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

sealed class DeviceType {
    data object Desktop : DeviceType()
    data object Tablet : DeviceType()
    data object Mobile : DeviceType()
}

data class PlatformConfiguration(
    val type: DeviceType,

    val screenSizeDp: DpSize, // 👈 better than raw Size
    val density: Float,
    val fontScale: Float,

    val isLandscape: Boolean,
    val isRTL: Boolean
)


val PlatformConfiguration.isMobile get() = type is DeviceType.Mobile
val PlatformConfiguration.isTablet get() = type is DeviceType.Tablet
val PlatformConfiguration.isDesktop get() = type is DeviceType.Desktop
val PlatformConfiguration.isWideScreen: Boolean
    get() = screenSizeDp.width > 600.dp

@Composable
expect fun rememberPlatformConfiguration(): PlatformConfiguration