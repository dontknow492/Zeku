package org.ghost.zeku.ui.theme

import android.os.Build
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.Contrast
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import timber.log.Timber


sealed class AppTheme(val name: String, val seedColor: Color) {
    object ForestGreen : AppTheme("Forest Green", Color(0xFF68A500))
    object SkyBlue : AppTheme("Sky Blue", Color(0xFFCBDDEE))
    object CherryBlossom : AppTheme("Cherry Blossom", Color(0xFFEC6094))
    object CrimsonRed : AppTheme("Crimson Red", Color(0xFF8B0300))
    object RoyalBlue : AppTheme("Royal Blue", Color(0xFF4C5CDC))
    object Periwinkle : AppTheme("Periwinkle", Color(0xFF769CDF))

    data class Custom(val customSeedColor: Color) : AppTheme("custom", customSeedColor)

    // An object to hold all available themes
    companion object {
        val default: AppTheme by lazy { SkyBlue }
        val themes by lazy {
            listOf(
                ForestGreen,
                SkyBlue,
                CherryBlossom,
                CrimsonRed,
                RoyalBlue,
                Periwinkle
            )
        }

        fun fromName(name: String?): AppTheme {
            return when (name) {
                "Forest Green" -> ForestGreen
                "Sky Blue" -> SkyBlue
                "Cherry Blossom" -> CherryBlossom
                "Crimson Red" -> CrimsonRed
                "Royal Blue" -> RoyalBlue
                "Periwinkle" -> Periwinkle
                else -> SkyBlue
            }
        }
    }
}


/**
 * A robust and optimized theme Composable for the Zeku app, powered by material-kolor.
 *
 * This function uses `rememberDynamicMaterialThemeState` to handle all color generation,
 * including dynamic colors, seed colors, AMOLED mode, and contrast levels.
 *
 * @param appTheme The predefined theme to use as a fallback. Defaults to `AppTheme.default`.
 * @param darkTheme Whether the theme should be in dark mode. Defaults to the system setting.
 * @param isAmoled If true and `darkTheme` is also true, sets the background and surface to pure black.
 * @param contrastLevel The desired contrast level for the generated theme (e.g., for accessibility).
 * @param dynamicColor A flag to enable or disable Material You dynamic coloring.
 * @param content The content to be displayed within the theme.
 */
@Composable
fun ZekuTheme(
    appTheme: AppTheme = AppTheme.SkyBlue,
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    contrastLevel: Double = Contrast.Default.value,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    Timber.d("Appearance: $darkTheme, AMOLED: $isAmoled, Contrast: $contrastLevel, Dynamic: $dynamicColor, Theme: ${appTheme.name}")
    Timber.d("Appearance: ${AppTheme.themes}")

    val state = when {
        // Condition for using system-level dynamic color
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // This prevents re-calculation on every recomposition.
            val colorScheme = remember(context, darkTheme) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            rememberDynamicMaterialThemeState(
                primary = colorScheme.primary,
                isDark = darkTheme,
                isAmoled = isAmoled,
                contrastLevel = contrastLevel,
                specVersion = ColorSpec.SpecVersion.SPEC_2025
            )
        }

        // Fallback condition for older devices or when dynamic color is off
        else -> {
            rememberDynamicMaterialThemeState(
                seedColor = appTheme.seedColor,
                isDark = darkTheme,
                isAmoled = isAmoled,
                contrastLevel = contrastLevel,
                specVersion = ColorSpec.SpecVersion.SPEC_2025
            )
        }
    }




    DynamicMaterialTheme(
        state = state,
        animate = true,
        animationSpec = tween(durationMillis = 1000),
        typography = Typography,
        content = content,
    )
}