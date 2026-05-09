package com.ghost.zeku.presentation.components.media.settings


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference
import com.ghost.zeku.presentation.components.media.GridStyle
import com.ghost.zeku.presentation.components.media.MediaDisplayMode
import com.ghost.zeku.presentation.components.media.list.ListCardConfig
import com.ghost.zeku.presentation.components.media.list.ListCardLayout
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.PosterLayout
import com.ghost.zeku.presentation.components.settings.SettingSectionTitle
import com.ghost.zeku.presentation.components.settings.SettingSlider
import com.ghost.zeku.presentation.components.settings.SettingToggleRow
import com.ghost.zeku.presentation.components.settings.SettingsCard

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun DisplaySettingsPanel(
    displayPreferences: MediaDisplayPreference,
    onMediaDisplayPreferenceChange: (MediaDisplayPreference) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    BoxWithConstraints(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {

        val isDesktop = maxWidth >= 900.dp
        val isTablet = maxWidth >= 600.dp

        if (isDesktop) {

            /* ---------------------------------------------------------- */
            /* DESKTOP LAYOUT */
            /* ---------------------------------------------------------- */

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                /* ---------------------------------------------------------- */
                /* SIDEBAR */
                /* ---------------------------------------------------------- */

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    shape = RoundedCornerShape(32.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {

                        SettingsHeader()

                        ModeSelectionSection(
                            displayPreference = displayPreferences,
                            onModeChanged = {
                                onMediaDisplayPreferenceChange(
                                    displayPreferences.copy(
                                        mode = it
                                    )
                                )
                            }
                        )

                        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                        QuickInfoCard(
                            displayPreference = displayPreferences
                        )
                    }
                }

                /* ---------------------------------------------------------- */
                /* SETTINGS */
                /* ---------------------------------------------------------- */

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(32.dp),
                    tonalElevation = 2.dp
                ) {

                    DetailedSettingsSection(
                        displayPreference = displayPreferences,
                        onMediaDisplayPreferenceChange = onMediaDisplayPreferenceChange,
                        isDesktop = true
                    )
                }
            }
        } else {

            /* ---------------------------------------------------------- */
            /* MOBILE/TABLET */
            /* ---------------------------------------------------------- */

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = if (isTablet) 24.dp else 16.dp,
                        vertical = 20.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                SettingsHeader()

                ModeSelectionSection(
                    displayPreference = displayPreferences,
                    onModeChanged = {
                        onMediaDisplayPreferenceChange(
                            displayPreferences.copy(
                                mode = it
                            )
                        )
                    }
                )

                DetailedSettingsSection(
                    displayPreference = displayPreferences,
                    onMediaDisplayPreferenceChange = onMediaDisplayPreferenceChange,
                    isDesktop = false
                )
            }
        }
    }
}

/* ---------------------------------------------------------- */
/* HEADER */
/* ---------------------------------------------------------- */

@Composable
private fun SettingsHeader() {

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        Text(
            text = "Display Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Customize how your media library looks and feels",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* ---------------------------------------------------------- */
/* MODE SELECTION */
/* ---------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSelectionSection(
    displayPreference: MediaDisplayPreference,
    onModeChanged: (MediaDisplayMode) -> Unit
) {

    ElevatedCard(
        shape = RoundedCornerShape(28.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Rounded.DashboardCustomize,
                        contentDescription = null
                    )
                }

                Column {

                    Text(
                        text = "Display Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Choose how media items are presented",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {

                SegmentedButton(
                    selected = displayPreference.mode == MediaDisplayMode.PosterGrid,
                    onClick = {
                        if (displayPreference.mode != MediaDisplayMode.PosterGrid) {
                            onModeChanged(
                                MediaDisplayMode.PosterGrid
                            )
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 0,
                        count = 2
                    ),
                    icon = {

                        Icon(
                            imageVector = Icons.Rounded.GridView,
                            contentDescription = null
                        )
                    }
                ) {

                    Text("Grid")
                }

                SegmentedButton(
                    selected = displayPreference.mode == MediaDisplayMode.List,
                    onClick = {
                        if (displayPreference.mode != MediaDisplayMode.List) {
                            onModeChanged(
                                MediaDisplayMode.List
                            )
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 1,
                        count = 2
                    ),
                    icon = {

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ViewList,
                            contentDescription = null
                        )
                    }
                ) {

                    Text("List")
                }
            }


            AnimatedContent(
                targetState = displayPreference.mode,
                label = "ModeDescription"
            ) { mode ->

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.45f
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = if (mode == MediaDisplayMode.PosterGrid) {
                                Icons.Rounded.GridView
                            } else {
                                Icons.AutoMirrored.Rounded.ViewList
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Column {

                            Text(
                                text = if (mode == MediaDisplayMode.PosterGrid) {
                                    "Poster Grid"
                                } else {
                                    "Media List"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = if (mode == MediaDisplayMode.PosterGrid) {
                                    "Visual browsing focused experience"
                                } else {
                                    "Dense information focused layout"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------- */
/* QUICK INFO */
/* ---------------------------------------------------------- */

@Composable
private fun QuickInfoCard(
    displayPreference: MediaDisplayPreference
) {

    ElevatedCard(
        shape = RoundedCornerShape(28.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Current Configuration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            when (displayPreference.mode) {

                MediaDisplayMode.PosterGrid -> {

                    QuickInfoRow(
                        label = "Mode",
                        value = "Grid"
                    )

                    QuickInfoRow(
                        label = "Spacing",
                        value = "${displayPreference.gridSpacing.value}dp"
                    )
                }

                MediaDisplayMode.List -> {

                    QuickInfoRow(
                        label = "Mode",
                        value = "List"
                    )

                    QuickInfoRow(
                        label = "Variant",
                        value = displayPreference.listCardLayout.name
                    )

                    QuickInfoRow(
                        label = "Poster Aspect Ratio",
                        value = "${displayPreference.posterConfig.aspectRatio} Ratio"
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickInfoRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* ---------------------------------------------------------- */
/* SETTINGS CONTENT */
/* ---------------------------------------------------------- */

@Composable
private fun DetailedSettingsSection(
    displayPreference: MediaDisplayPreference,
    onMediaDisplayPreferenceChange: (MediaDisplayPreference) -> Unit,
    isDesktop: Boolean
) {
    val isGrid = displayPreference.mode == MediaDisplayMode.PosterGrid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
//                horizontal = if (isDesktop) 28.dp else 0.dp,
                vertical = if (isDesktop) 28.dp else 0.dp
            ),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {

        AnimatedContent(
            targetState = isGrid,
            label = "DetailedSettingsTransition"
        ) { mode ->

            when (mode) {
                true -> GridSettings(
                    displayPreference = displayPreference,
                    onMediaDisplayPreferenceChange = onMediaDisplayPreferenceChange,
                )

                false -> ListSettings(
                    displayPreference = displayPreference,
                    onMediaDisplayPreferenceChange = onMediaDisplayPreferenceChange,
                )

            }
        }
    }
}


@Composable
fun GridSettings(
    displayPreference: MediaDisplayPreference,
    onMediaDisplayPreferenceChange: (MediaDisplayPreference) -> Unit,
) {
    val config = displayPreference.posterConfig
    val onConfigChange: (PosterConfig) -> Unit = {
        onMediaDisplayPreferenceChange(
            displayPreference.copy(
                posterConfig = it
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        /* ---------------------------------------------------------- */
        /* QUICK PRESETS */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Quick Presets",
            subtitle = "Instantly switch between layouts",
            icon = Icons.Rounded.AutoAwesome
        ) {

            PresetSelector(
                selected = displayPreference.posterLayout,
                onSelected = { layout ->
                    onMediaDisplayPreferenceChange(
                        displayPreference.copy(posterLayout = layout)
                    )
                }
            )
        }

        /* ---------------------------------------------------------- */
        /* CARD STYLE */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Card Appearance",
            subtitle = "Visual appearance of poster cards",
            icon = Icons.Rounded.Palette
        ) {

            SettingSectionTitle("Layout Style")

            EnumChipRow(
                current = displayPreference.posterLayout,
                options = listOf(
                    PosterLayout.Minimal,
                    PosterLayout.Overlay,
                    PosterLayout.Modern,
                    PosterLayout.Compact
                ),
                label = {
                    when (it) {
                        PosterLayout.Minimal -> "Minimal"
                        PosterLayout.Overlay -> "Overlay"
                        PosterLayout.Modern -> "Modern"
                        PosterLayout.Compact -> "Compact"
                    }
                },
                onSelected = {
                    onMediaDisplayPreferenceChange(
                        displayPreference.copy(
                            posterLayout = it
                        )
                    )

                }
            )

            Spacer(Modifier.height(20.dp))

            SettingSlider(
                title = "Corner Radius",
                value = config.cornerRadius.value,
                range = 0f..32f,
                suffix = "dp",
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            cornerRadius = it.dp
                        )
                    )
                }
            )

            Spacer(Modifier.height(16.dp))

            SettingSlider(
                title = "Hover Zoom",
                value = config.scaleOnHover,
                range = 1f..1.20f,
                steps = 3,
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            scaleOnHover = it
                        )
                    )

                }
            )

            Spacer(Modifier.height(16.dp))

            SettingToggleRow(
                title = "Enable Hover Zoom",
                subtitle = "Scale posters on hover",
                checked = config.enableHover,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            enableGlow = it
                        )
                    )

                }
            )
        }

        /* ---------------------------------------------------------- */
        /* GRID */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Grid Layout",
            subtitle = "Configure spacing and sizing",
            icon = Icons.Rounded.GridView
        ) {

            SettingSectionTitle("Grid Type")

            val isAdaptive = displayPreference.gridStyle == GridStyle.Adaptive

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {

                SegmentedButton(
                    selected = isAdaptive,
                    onClick = {
                        onMediaDisplayPreferenceChange(
                            displayPreference.copy(
                                gridStyle = GridStyle.Adaptive
                            )
                        )
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 0,
                        count = 2
                    )
                ) {
                    Text("Adaptive")
                }

                SegmentedButton(
                    selected = !isAdaptive,
                    onClick = {
                        onMediaDisplayPreferenceChange(
                            displayPreference.copy(
                                gridStyle = GridStyle.Fixed
                            )
                        )
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 1,
                        count = 2
                    )
                ) {
                    Text("Fixed")
                }
            }

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(isAdaptive) {

                SettingSlider(
                    title = "Minimum Card Width",
                    value = displayPreference.gridMinSize.value,
                    range = 90f..260f,
                    suffix = "dp",
                    onValueChange = {
                        onMediaDisplayPreferenceChange(
                            displayPreference.copy(
                                gridMinSize = it.dp
                            )
                        )
                    }
                )
            }

            AnimatedVisibility(!isAdaptive) {

                SettingSlider(
                    title = "Columns",
                    value = displayPreference.gridCount.toFloat(),
                    range = 2f..8f,
                    steps = 5,
                    onValueChange = {
                        onMediaDisplayPreferenceChange(
                            displayPreference.copy(
                                gridCount = it.toInt()
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            SettingSlider(
                title = "Grid Spacing",
                value = displayPreference.gridSpacing.value,
                range = 0f..32f,
                suffix = "dp",
                onValueChange = {
                    onMediaDisplayPreferenceChange(
                        displayPreference.copy(
                            gridSpacing = it.dp
                        )
                    )
                }
            )
        }

        /* ---------------------------------------------------------- */
        /* IMAGE */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Poster Image",
            subtitle = "Image ratio and scaling",
            icon = Icons.Rounded.Image
        ) {

            SettingSectionTitle("Aspect Ratio")

            EnumChipRow(
                current = config.aspectRatio,
                options = listOf(
                    2f / 3f,
                    1f,
                    16f / 9f
                ),
                label = {
                    when (it) {
                        2f / 3f -> "Poster"
                        1f -> "Square"
                        else -> "Wide"
                    }
                },
                onSelected = { ratio ->
                    onConfigChange(
                        config.copy(
                            aspectRatio = ratio

                        )
                    )

                }
            )
        }

        /* ---------------------------------------------------------- */
        /* TYPOGRAPHY */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Typography",
            subtitle = "Adjust title visibility",
            icon = Icons.Rounded.TextFields
        ) {

            SettingSlider(
                title = "Title Lines",
                value = config.maxTitleLines.toFloat(),
                range = 1f..4f,
                steps = 2,
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            maxTitleLines = it.toInt()
                        )
                    )

                }
            )

            Spacer(Modifier.height(16.dp))

            SettingToggleRow(
                title = "Show Subtitle",
                subtitle = "Display additional info",
                checked = config.showSubtitle,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            showSubtitle = it

                        )
                    )

                }
            )
        }

        /* ---------------------------------------------------------- */
        /* BADGES */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Badges & Metadata",
            subtitle = "Overlay information",
            icon = Icons.Rounded.LocalMovies
        ) {

            SettingToggleRow(
                title = "Show Score",
                subtitle = "Display ratings",
                checked = config.showScore,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            showScore = it
                        )
                    )

                }
            )


            SettingToggleRow(
                title = "Show Progress",
                subtitle = "Episode progress indicator",
                checked = config.showProgress,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            showProgress = it

                        )
                    )

                }
            )
        }

        /* ---------------------------------------------------------- */
        /* NSFW */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "NSFW Protection",
            subtitle = "Sensitive content settings",
            icon = Icons.Rounded.Security
        ) {

            SettingToggleRow(
                title = "Blur NSFW Content",
                subtitle = "Hide explicit media",
                checked = config.enableBlurNsfw,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            enableBlurNsfw = it
                        )
                    )

                }
            )
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
private fun ListSettings(
    displayPreference: MediaDisplayPreference,
    onMediaDisplayPreferenceChange: (MediaDisplayPreference) -> Unit,
) {
    val onConfigChange: (ListCardConfig) -> Unit = {
        onMediaDisplayPreferenceChange(
            displayPreference.copy(
                listConfig = it
            )
        )
    }

    val config = displayPreference.listConfig

    val mode = MediaDisplayMode.List

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        /* ---------------------------------------------------------- */
        /* VARIANT */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "List Style",
            subtitle = "Choose the overall list appearance",
            icon = Icons.Rounded.ViewAgenda
        ) {

            EnumChipRow(
                current = displayPreference.listCardLayout,
                options = ListCardLayout.entries,
                label = {
                    it.name
                },
                onSelected = { variant ->
                    onMediaDisplayPreferenceChange(
                        displayPreference.copy(
                            listCardLayout = variant
                        )
                    )
                }
            )
        }

        /* ---------------------------------------------------------- */
        /* LAYOUT */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Layout",
            subtitle = "Spacing, sizing and structure",
            icon = Icons.Rounded.SpaceDashboard
        ) {

            SettingSlider(
                title = "Item Spacing",
                value = displayPreference.listSpacing.value,
                range = 0f..32f,
                suffix = "dp",
                onValueChange = {
                    onMediaDisplayPreferenceChange(
                        displayPreference.copy(
                            listSpacing = it.dp
                        )
                    )
                }
            )

            Spacer(Modifier.height(18.dp))

            SettingSlider(
                title = "Poster Width",
                value = config.imageWidth.value,
                range = 50f..180f,
                suffix = "dp",
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            imageWidth = it.dp
                        )
                    )

                }
            )

            Spacer(Modifier.height(18.dp))

            SettingSlider(
                title = "Corner Radius",
                value = config.cornerRadius.value,
                range = 0f..36f,
                suffix = "dp",
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            cornerRadius = it.dp

                        )
                    )

                }
            )
        }

        /* ---------------------------------------------------------- */
        /* INTERACTION */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Interaction",
            subtitle = "Hover and animation behavior",
            icon = Icons.Rounded.TouchApp
        ) {

            SettingToggleRow(
                title = "Enable Hover Effects",
                subtitle = "Animate cards on hover",
                checked = config.enableHover,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            enableHover = it

                        )

                    )
                }
            )

            AnimatedVisibility(config.enableHover) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    Spacer(Modifier.height(12.dp))

                    SettingSlider(
                        title = "Hover Scale",
                        value = config.scaleOnHover,
                        range = 1f..1.10f,
                        onValueChange = {
                            onConfigChange(
                                config.copy(

                                    scaleOnHover = it

                                )
                            )

                        }
                    )

                    SettingSlider(
                        title = "Pressed Scale",
                        value = config.scaleOnPress,
                        range = 0.85f..1f,
                        onValueChange = {
                            onConfigChange(
                                config.copy(
                                    scaleOnPress = it

                                )
                            )

                        }
                    )

                    SettingSlider(
                        title = "Hover Elevation",
                        value = config.elevation.value,
                        range = 0f..24f,
                        suffix = "dp",
                        onValueChange = {
                            onConfigChange(
                                config.copy(
                                    elevation = it.dp

                                )
                            )

                        }
                    )
                }
            }
        }

        /* ---------------------------------------------------------- */
        /* CONTENT */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Content",
            subtitle = "Control visible information",
            icon = Icons.AutoMirrored.Rounded.Article
        ) {

            SettingToggleRow(
                title = "Show Description",
                subtitle = "Display synopsis text",
                checked = config.showDescription,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            showDescription = it

                        )
                    )

                }
            )

            AnimatedVisibility(config.showDescription) {

                Column {

                    Spacer(Modifier.height(16.dp))

                    SettingSlider(
                        title = "Description Lines",
                        value = config.maxDescriptionLines.toFloat(),
                        range = 1f..8f,
                        steps = 6,
                        onValueChange = {
                            onConfigChange(
                                config.copy(
                                    maxDescriptionLines = it.toInt()
                                )
                            )

                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SettingToggleRow(
                title = "Show Genres",
                subtitle = "Display media genres",
                checked = config.showGenres,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            showGenres = it

                        )
                    )

                }
            )

            Spacer(Modifier.height(16.dp))

            SettingToggleRow(
                title = "Show Score",
                subtitle = "Display ratings",
                checked = config.showScore,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            showScore = it

                        )
                    )

                }
            )

            Spacer(Modifier.height(16.dp))

            SettingToggleRow(
                title = "Show Progress",
                subtitle = "Display watch/read progress",
                checked = config.showProgress,
                onCheckedChange = {
                    onConfigChange(
                        config.copy(
                            showProgress = it

                        )
                    )

                }
            )
        }

        /* ---------------------------------------------------------- */
        /* IMAGE */
        /* ---------------------------------------------------------- */

        SettingsCard(
            title = "Poster Image",
            subtitle = "Poster scaling and appearance",
            icon = Icons.Rounded.Image
        ) {

            SettingSlider(
                title = "Poster Corner Radius",
                value = config.cornerRadius.value,
                range = 0f..32f,
                suffix = "dp",
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            cornerRadius = it.dp

                        )
                    )

                }
            )

            Spacer(Modifier.height(18.dp))

            SettingSlider(
                title = "Hover Zoom Scale",
                value = config.scaleOnHover,
                range = 1f..1.20f,
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            scaleOnHover = it
                        )
                    )

                }
            )
        }

        /* ---------------------------------------------------------- */
        /* NSFW */
        /* ---------------------------------------------------------- */
    }
}

// --- Helper Composables ---


@Composable
private fun <T> EnumChipRow(
    current: T,
    options: List<T>,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        options.forEach { option ->

            FilterChip(
                selected = current == option,
                onClick = { onSelected(option) },
                label = {
                    Text(label(option))
                }
            )
        }
    }
}

@Composable
private fun PresetSelector(
    selected: PosterLayout,
    onSelected: (PosterLayout) -> Unit
) {

    EnumChipRow(
        current = selected,
        options = listOf(
            PosterLayout.Modern,
            PosterLayout.Overlay,
            PosterLayout.Minimal,
            PosterLayout.Compact
        ),
        label = {
            when (it) {
                PosterLayout.Modern -> "Modern"
                PosterLayout.Overlay -> "Cinematic"
                PosterLayout.Minimal -> "Minimal"
                PosterLayout.Compact -> "Compact"
            }
        },
        onSelected = onSelected
    )
}


@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

@Composable
private fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


@Preview
@Composable
private fun DisplaySettingPanelPreview() {
    DisplaySettingsPanel(
        displayPreferences = MediaDisplayPreference(),
        onMediaDisplayPreferenceChange = { },
        modifier = Modifier,
        contentPadding = PaddingValues(0.dp)
    )
}