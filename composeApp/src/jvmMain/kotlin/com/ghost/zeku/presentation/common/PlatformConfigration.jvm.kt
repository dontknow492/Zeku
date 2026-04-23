// desktopMain

package com.ghost.zeku.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
actual fun rememberPlatformConfiguration(): PlatformConfiguration {
    val density = LocalDensity.current

    // ⚠️ Compose Desktop doesn’t give direct config like Android
    // You usually pass WindowState from your app root
    val windowSize = LocalWindowInfo.current.containerSize

    val widthDp = with(density) { windowSize.width.toDp() }
    val heightDp = with(density) { windowSize.height.toDp() }

    val deviceType = when {
        widthDp >= 1024.dp -> DeviceType.Desktop
        widthDp >= 600.dp -> DeviceType.Tablet
        else -> DeviceType.Mobile
    }

    return PlatformConfiguration(
        type = deviceType,
        screenSizeDp = DpSize(widthDp, heightDp),
        density = density.density,
        fontScale = density.fontScale,
        isLandscape = widthDp > heightDp,
        isRTL = false // Desktop usually LTR unless you handle manually
    )
}