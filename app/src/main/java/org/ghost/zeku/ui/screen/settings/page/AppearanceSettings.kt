package org.ghost.zeku.ui.screen.settings.page

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ghost.zeku.R
import org.ghost.zeku.core.enum.ThemeMode
import org.ghost.zeku.ui.component.SettingItem
import org.ghost.zeku.ui.component.SettingTitle
import org.ghost.zeku.ui.component.SwitchSettingItem
import org.ghost.zeku.ui.component.ThemeItem
import org.ghost.zeku.ui.screen.settings.AppearanceSettingsState
import org.ghost.zeku.ui.screen.settings.BackButton
import org.ghost.zeku.ui.theme.AppTheme
import org.ghost.zeku.ui.theme.ZekuTheme

sealed interface AppearanceSettingsEvent {
    data class OnThemeModeChange(val themeMode: ThemeMode) : AppearanceSettingsEvent
    data class OnAccentColorChange(val accentColor: Color) : AppearanceSettingsEvent
    data class OnThemeChange(val theme: AppTheme) : AppearanceSettingsEvent
    data class OnAmoledChange(val amoled: Boolean) : AppearanceSettingsEvent
    data class OnDynamicColorChange(val dynamicColor: Boolean) : AppearanceSettingsEvent
    data class OnHighContrastChange(val highContrast: Float) : AppearanceSettingsEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    modifier: Modifier = Modifier,
    state: AppearanceSettingsState,
    eventHandler: (AppearanceSettingsEvent) -> Unit,
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
            SettingTitle(stringResource(R.string.settings_appearance_title))
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ThemeMode.entries.forEachIndexed { index, theme ->
                    SegmentedButton(
                        onClick = { eventHandler(AppearanceSettingsEvent.OnThemeModeChange(theme)) },
                        selected = theme == state.themeMode,
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ThemeMode.entries.size
                        ),
                        label = { Text(theme.name, maxLines = 1) }
                    )
                }
            }
            ThemeList(
                selectedTheme = state.theme,
                onSelectionChange = { theme ->
                    eventHandler(AppearanceSettingsEvent.OnThemeChange(theme))
                }
            )
            // ### Accent Color ###
            SettingItem(
                title = stringResource(R.string.settings_appearance_accent_color_title),
                description = stringResource(R.string.settings_appearance_accent_color_description),
                icon = ImageVector.vectorResource(R.drawable.rounded_colors_24),
                onClick = { /* TODO: Show color picker dialog */ }
            )

            // ### AMOLED Mode ###
            SwitchSettingItem(
                title = stringResource(R.string.settings_appearance_amoled_mode_title),
                description = stringResource(R.string.settings_appearance_amoled_mode_description),
                icon = ImageVector.vectorResource(R.drawable.rounded_brightness_6_24),
                checked = state.amoled,
                onSelectionChange = { eventHandler(AppearanceSettingsEvent.OnAmoledChange(it)) }
            )

            // ### Dynamic Color ###
            SwitchSettingItem(
                title = stringResource(R.string.settings_appearance_dynamic_color_title),
                description = stringResource(R.string.settings_appearance_dynamic_color_description),
                icon = ImageVector.vectorResource(R.drawable.rounded_dynamic_form_24),
                checked = state.dynamicColor,
                onSelectionChange = { eventHandler(AppearanceSettingsEvent.OnDynamicColorChange(it)) }
            )

            // ### High Contrast ###
            val contrastPercentage = (state.highContrast * 100).toInt()
            val contrastValue = stringResource(
                R.string.settings_appearance_high_contrast_value_format,
                contrastPercentage
            )
            SettingItem(
                title = stringResource(R.string.settings_appearance_high_contrast_title),
                description = "${stringResource(R.string.settings_appearance_high_contrast_description)}\n$contrastValue",
                icon = ImageVector.vectorResource(R.drawable.rounded_contrast_24),
                onClick = { /* TODO: Show contrast adjustment dialog/slider */ }
            )
        }
    }
}

@Composable
fun ThemeList(
    modifier: Modifier = Modifier,
    selectedTheme: AppTheme,
    onSelectionChange: (AppTheme) -> Unit
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTheme.themes.forEach { theme ->
            ThemeItem(
                theme = theme,
                selected = theme == selectedTheme,
                onSelectionChange = onSelectionChange
            )
        }
    }
}

@Preview
@Composable
fun AppearanceSettingsPreview() {
    val state = AppearanceSettingsState(
        themeMode = ThemeMode.SYSTEM,
        accentColor = Color.Blue,
        theme = AppTheme.SkyBlue,
        amoled = false,
        dynamicColor = true,
        highContrast = 0f
    )
    ZekuTheme {
        AppearanceSettings(
            state = state,
            eventHandler = {},
            onBackClick = {}
        )
    }
}
