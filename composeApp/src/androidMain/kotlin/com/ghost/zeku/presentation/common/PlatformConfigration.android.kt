// androidMain

package com.ghost.zeku.presentation.common

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
actual fun rememberPlatformConfiguration(): PlatformConfiguration {
    val config = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidth = config.screenWidthDp
    val screenHeight = config.screenHeightDp

    val deviceType = when {
        screenWidth >= 840 -> DeviceType.Desktop   // foldables / large tablets
        screenWidth >= 600 -> DeviceType.Tablet
        else -> DeviceType.Mobile
    }

    return PlatformConfiguration(
        type = deviceType,
        screenSizeDp = DpSize(screenWidth.dp, screenHeight.dp),
        density = density.density,
        fontScale = density.fontScale,
        isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE,
        isRTL = config.layoutDirection == android.util.LayoutDirection.RTL
    )
}