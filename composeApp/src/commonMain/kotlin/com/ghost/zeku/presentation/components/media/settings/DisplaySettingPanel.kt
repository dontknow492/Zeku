package com.ghost.zeku.presentation.components.media.settings


//import com.ghost.zeku.presentation.components.media.list.ListConfig
//import com.ghost.zeku.presentation.components.media.list.MediaListCardVariant
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.settings.MediaDisplayPreference

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
//    BoxWithConstraints(
//        modifier = modifier.verticalScroll(rememberScrollState())
//    ) {
//
//        val isDesktop = maxWidth >= 900.dp
//        val isTablet = maxWidth >= 600.dp
//
//        if (isDesktop) {
//
//            /* ---------------------------------------------------------- */
//            /* DESKTOP LAYOUT */
//            /* ---------------------------------------------------------- */
//
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(contentPadding),
//                horizontalArrangement = Arrangement.spacedBy(24.dp)
//            ) {
//
//                /* ---------------------------------------------------------- */
//                /* SIDEBAR */
//                /* ---------------------------------------------------------- */
//
//                ElevatedCard(
//                    modifier = Modifier.fillMaxWidth().padding(24.dp),
//                    shape = RoundedCornerShape(32.dp)
//                ) {
//
//                    Column(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .padding(20.dp),
//                        verticalArrangement = Arrangement.spacedBy(24.dp)
//                    ) {
//
//                        SettingsHeader()
//
//                        ModeSelectionSection(
//                            displayPreference = displayPreferences,
//                            onModeChanged = {
//                                onMediaDisplayPreferenceChange(
//                                    displayPreferences.copy(
//                                        mode = it
//                                    )
//                                )
//                            }
//                        )
//
//                        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
//
//                        QuickInfoCard(
//                            displayPreference = displayPreferences
//                        )
//                    }
//                }
//
//                /* ---------------------------------------------------------- */
//                /* SETTINGS */
//                /* ---------------------------------------------------------- */
//
//                Surface(
//                    modifier = Modifier.weight(1f),
//                    shape = RoundedCornerShape(32.dp),
//                    tonalElevation = 2.dp
//                ) {
//
//                    DetailedSettingsSection(
//                        displayPreference = displayPreferences,
//                        onMediaDisplayPreferenceChange = onMediaDisplayPreferenceChange,
//                        isDesktop = true
//                    )
//                }
//            }
//        } else {
//
//            /* ---------------------------------------------------------- */
//            /* MOBILE/TABLET */
//            /* ---------------------------------------------------------- */
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(
//                        horizontal = if (isTablet) 24.dp else 16.dp,
//                        vertical = 20.dp
//                    ),
//                verticalArrangement = Arrangement.spacedBy(20.dp)
//            ) {
//
//                SettingsHeader()
//
//                ModeSelectionSection(
//                    displayPreference = displayPreferences,
//                    onModeChanged = {
//                        onMediaDisplayPreferenceChange(
//                            displayPreferences.copy(
//                                mode = it
//                            )
//                        )
//                    }
//                )
//
//                DetailedSettingsSection(
//                    displayPreference = displayPreferences,
//                    onMediaDisplayPreferenceChange = onMediaDisplayPreferenceChange,
//                    isDesktop = false
//                )
//            }
//        }
//    }
}

/* ---------------------------------------------------------- */
/* HEADER */
/* ---------------------------------------------------------- */

//@Composable
//private fun SettingsHeader() {
//
//    Column(
//        verticalArrangement = Arrangement.spacedBy(6.dp)
//    ) {
//
//        Text(
//            text = "Display Settings",
//            style = MaterialTheme.typography.headlineSmall,
//            fontWeight = FontWeight.Bold
//        )
//
//        Text(
//            text = "Customize how your media library looks and feels",
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}
//
///* ---------------------------------------------------------- */
///* MODE SELECTION */
///* ---------------------------------------------------------- */
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun ModeSelectionSection(
//    displayPreference: MediaDisplayPreference,
//    onModeChanged: (MediaDisplayMode) -> Unit
//) {
//
//    ElevatedCard(
//        shape = RoundedCornerShape(28.dp)
//    ) {
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp),
//            verticalArrangement = Arrangement.spacedBy(18.dp)
//        ) {
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//                Box(
//                    modifier = Modifier
//                        .size(44.dp)
//                        .clip(CircleShape)
//                        .background(
//                            MaterialTheme.colorScheme.secondaryContainer
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//
//                    Icon(
//                        imageVector = Icons.Rounded.DashboardCustomize,
//                        contentDescription = null
//                    )
//                }
//
//                Column {
//
//                    Text(
//                        text = "Display Mode",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.SemiBold
//                    )
//
//                    Text(
//                        text = "Choose how media items are presented",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            SingleChoiceSegmentedButtonRow(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//
//                SegmentedButton(
//                    selected = displayPreference.mode == MediaDisplayMode.PosterGrid,
//                    onClick = {
//                        if (displayPreference.mode != MediaDisplayMode.PosterGrid) {
//                            onModeChanged(
//                                MediaDisplayMode.PosterGrid
//                            )
//                        }
//                    },
//                    shape = SegmentedButtonDefaults.itemShape(
//                        index = 0,
//                        count = 2
//                    ),
//                    icon = {
//
//                        Icon(
//                            imageVector = Icons.Rounded.GridView,
//                            contentDescription = null
//                        )
//                    }
//                ) {
//
//                    Text("Grid")
//                }
//
//                SegmentedButton(
//                    selected = displayPreference.mode == MediaDisplayMode.List,
//                    onClick = {
//                        if (displayPreference.mode != MediaDisplayMode.List) {
//                            onModeChanged(
//                                MediaDisplayMode.List
//                            )
//                        }
//                    },
//                    shape = SegmentedButtonDefaults.itemShape(
//                        index = 1,
//                        count = 2
//                    ),
//                    icon = {
//
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Rounded.ViewList,
//                            contentDescription = null
//                        )
//                    }
//                ) {
//
//                    Text("List")
//                }
//            }
//
//
//            AnimatedContent(
//                targetState = displayPreference.mode,
//                label = "ModeDescription"
//            ) { mode ->
//
//                Surface(
//                    color = MaterialTheme.colorScheme.surfaceVariant.copy(
//                        alpha = 0.45f
//                    ),
//                    shape = RoundedCornerShape(20.dp)
//                ) {
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        horizontalArrangement = Arrangement.spacedBy(14.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//
//                        Icon(
//                            imageVector = if (mode == MediaDisplayMode.PosterGrid) {
//                                Icons.Rounded.GridView
//                            } else {
//                                Icons.AutoMirrored.Rounded.ViewList
//                            },
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//
//                        Column {
//
//                            Text(
//                                text = if (mode == MediaDisplayMode.PosterGrid) {
//                                    "Poster Grid"
//                                } else {
//                                    "Media List"
//                                },
//                                style = MaterialTheme.typography.bodyLarge,
//                                fontWeight = FontWeight.SemiBold
//                            )
//
//                            Text(
//                                text = if (mode == MediaDisplayMode.PosterGrid) {
//                                    "Visual browsing focused experience"
//                                } else {
//                                    "Dense information focused layout"
//                                },
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
///* ---------------------------------------------------------- */
///* QUICK INFO */
///* ---------------------------------------------------------- */
//
//@Composable
//private fun QuickInfoCard(
//    displayPreference: MediaDisplayPreference
//) {
//
//    ElevatedCard(
//        shape = RoundedCornerShape(28.dp)
//    ) {
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(18.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//
//            Text(
//                text = "Current Configuration",
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            when (displayPreference.mode) {
//
//                MediaDisplayMode.PosterGrid -> {
//
//                    QuickInfoRow(
//                        label = "Mode",
//                        value = "Grid"
//                    )
//
//                    QuickInfoRow(
//                        label = "Spacing",
//                        value = "${displayPreference.gridSpacing.value}dp"
//                    )
//                }
//
//                MediaDisplayMode.List -> {
//
//                    QuickInfoRow(
//                        label = "Mode",
//                        value = "List"
//                    )
//
//                    QuickInfoRow(
//                        label = "Variant",
//                        value = displayPreference.listCardLayout.name
//                    )
//
//                    QuickInfoRow(
//                        label = "Poster Width",
//                        value = "${displayPreference.posterConfig.content.width}dp"
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun QuickInfoRow(
//    label: String,
//    value: String
//) {
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        Text(
//            text = value,
//            style = MaterialTheme.typography.bodyMedium,
//            fontWeight = FontWeight.SemiBold
//        )
//    }
//}
//
///* ---------------------------------------------------------- */
///* SETTINGS CONTENT */
///* ---------------------------------------------------------- */
//
//@Composable
//private fun DetailedSettingsSection(
//    displayPreference: MediaDisplayPreference,
//    onMediaDisplayPreferenceChange: (MediaDisplayPreference) -> Unit,
//    isDesktop: Boolean
//) {
//    val isGrid = displayPreference.mode == MediaDisplayMode.PosterGrid
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(
////                horizontal = if (isDesktop) 28.dp else 0.dp,
//                vertical = if (isDesktop) 28.dp else 0.dp
//            ),
//        verticalArrangement = Arrangement.spacedBy(22.dp)
//    ) {
//
//        AnimatedContent(
//            targetState = isGrid,
//            label = "DetailedSettingsTransition"
//        ) { mode ->
//
//            when (mode) {
//                true -> GridSettings(
//                    displayPreference = displayPreference,
//                    onMediaDisplayPreferenceChange = onMediaDisplayPreferenceChange,
//                )
//
//                false -> ListSettings(
//                    displayPreference = displayPreference,
//                    onMediaDisplayPreferenceChange = onMediaDisplayPreferenceChange,
//                )
//
//            }
//        }
//    }
//}
//
//
//@Composable
//fun GridSettings(
//    displayPreference: MediaDisplayPreference,
//    onMediaDisplayPreferenceChange: (MediaDisplayPreference) -> Unit,
//) {
//    val config = displayPreference.posterConfig
//    val onConfigChange: (PosterConfig) -> Unit = {
//        onMediaDisplayPreferenceChange(
//            displayPreference.copy(
//                posterConfig = it
//            )
//        )
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(0.dp),
//        verticalArrangement = Arrangement.spacedBy(18.dp)
//    ) {
//
//        /* ---------------------------------------------------------- */
//        /* QUICK PRESETS */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Quick Presets",
//            subtitle = "Instantly switch between layouts",
//            icon = Icons.Rounded.AutoAwesome
//        ) {
//
//            PresetSelector(
//                selected = config.layout,
//                onSelected = { layout ->
//                    onConfigChange(
//                        config.copy(
//                            layout = layout
//                        )
//                    )
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* CARD STYLE */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Card Appearance",
//            subtitle = "Visual appearance of poster cards",
//            icon = Icons.Rounded.Palette
//        ) {
//
//            SettingSectionTitle("Layout Style")
//
//            EnumChipRow(
//                current = config.layout,
//                options = listOf(
//                    PosterLayout.Minimal,
//                    PosterLayout.Overlay,
//                    PosterLayout.Modern,
//                    PosterLayout.Compact
//                ),
//                label = {
//                    when (it) {
//                        PosterLayout.Minimal -> "Minimal"
//                        PosterLayout.Overlay -> "Overlay"
//                        PosterLayout.Modern -> "Modern"
//                        PosterLayout.Compact -> "Compact"
//                    }
//                },
//                onSelected = {
//                    onConfigChange(
//                        config.copy(layout = it)
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(20.dp))
//
//            SettingSlider(
//                title = "Corner Radius",
//                value = config.image.cornerRadius.value,
//                range = 0f..32f,
//                suffix = "dp",
//                onValueChange = {
//                    onConfigChange(
//                        config.copy(
//                            image = config.image.copy(
//                                cornerRadius = it.dp
//                            )
//
//                        )
//                    )
//                }
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            SettingSlider(
//                title = "Hover Zoom",
//                value = config.image.hoverZoomScale,
//                range = 1f..1.20f,
//                steps = 3,
//                onValueChange = {
//                    onConfigChange(
//                        config.copy(
//                            image = config.image.copy(
//                                hoverZoomScale = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            SettingToggleRow(
//                title = "Enable Hover Zoom",
//                subtitle = "Scale posters on hover",
//                checked = config.image.enableHoverZoom,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            image = config.image.copy(
//                                enableHoverZoom = it
//                            )
//                        )
//                    )
//
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* GRID */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Grid Layout",
//            subtitle = "Configure spacing and sizing",
//            icon = Icons.Rounded.GridView
//        ) {
//
//            SettingSectionTitle("Grid Type")
//
//            val isAdaptive = displayPreference.gridStyle == GridStyle.Adaptive
//
//            SingleChoiceSegmentedButtonRow(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//
//                SegmentedButton(
//                    selected = isAdaptive,
//                    onClick = {
//                        onMediaDisplayPreferenceChange(
//                            displayPreference.copy(
//                                gridStyle = GridStyle.Adaptive
//                            )
//                        )
//                    },
//                    shape = SegmentedButtonDefaults.itemShape(
//                        index = 0,
//                        count = 2
//                    )
//                ) {
//                    Text("Adaptive")
//                }
//
//                SegmentedButton(
//                    selected = !isAdaptive,
//                    onClick = {
//                        onMediaDisplayPreferenceChange(
//                            displayPreference.copy(
//                                gridStyle = GridStyle.Fixed
//                            )
//                        )
//                    },
//                    shape = SegmentedButtonDefaults.itemShape(
//                        index = 1,
//                        count = 2
//                    )
//                ) {
//                    Text("Fixed")
//                }
//            }
//
//            Spacer(Modifier.height(20.dp))
//
//            AnimatedVisibility(isAdaptive) {
//
//                SettingSlider(
//                    title = "Minimum Card Width",
//                    value = displayPreference.gridMinSize.value,
//                    range = 90f..260f,
//                    suffix = "dp",
//                    onValueChange = {
//                        onMediaDisplayPreferenceChange(
//                            displayPreference.copy(
//                                gridMinSize = it.dp
//                            )
//                        )
//                    }
//                )
//            }
//
//            AnimatedVisibility(!isAdaptive) {
//
//                SettingSlider(
//                    title = "Columns",
//                    value = displayPreference.gridCount.toFloat(),
//                    range = 2f..8f,
//                    steps = 5,
//                    onValueChange = {
//                        onMediaDisplayPreferenceChange(
//                            displayPreference.copy(
//                                gridCount = it.toInt()
//                            )
//                        )
//                    }
//                )
//            }
//
//            Spacer(Modifier.height(20.dp))
//
//            SettingSlider(
//                title = "Grid Spacing",
//                value = displayPreference.gridSpacing.value,
//                range = 0f..32f,
//                suffix = "dp",
//                onValueChange = {
//                    onMediaDisplayPreferenceChange(
//                        displayPreference.copy(
//                            gridSpacing = it.dp
//                        )
//                    )
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* IMAGE */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Poster Image",
//            subtitle = "Image ratio and scaling",
//            icon = Icons.Rounded.Image
//        ) {
//
//            SettingSectionTitle("Aspect Ratio")
//
//            EnumChipRow(
//                current = config.image.aspectRatio,
//                options = listOf(
//                    2f / 3f,
//                    1f,
//                    16f / 9f
//                ),
//                label = {
//                    when (it) {
//                        2f / 3f -> "Poster"
//                        1f -> "Square"
//                        else -> "Wide"
//                    }
//                },
//                onSelected = { ratio ->
//                    onConfigChange(
//                        config.copy(
//                            image = config.image.copy(
//                                aspectRatio = ratio
//                            )
//                        )
//                    )
//
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* TYPOGRAPHY */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Typography",
//            subtitle = "Adjust title visibility",
//            icon = Icons.Rounded.TextFields
//        ) {
//
//            SettingSlider(
//                title = "Title Lines",
//                value = config.content.titleMaxLines.toFloat(),
//                range = 1f..4f,
//                steps = 2,
//                onValueChange = {
//                    onConfigChange(
//                        config.copy(
//                            content = config.content.copy(
//                                titleMaxLines = it.toInt()
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            SettingToggleRow(
//                title = "Show Subtitle",
//                subtitle = "Display additional info",
//                checked = config.content.showSubtitle,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            content = config.content.copy(
//                                showSubtitle = it
//                            )
//                        )
//                    )
//
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* BADGES */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Badges & Metadata",
//            subtitle = "Overlay information",
//            icon = Icons.Rounded.LocalMovies
//        ) {
//
//            SettingToggleRow(
//                title = "Show Score",
//                subtitle = "Display ratings",
//                checked = config.badges.showScore,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            badges = config.badges.copy(
//                                showScore = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            SettingToggleRow(
//                title = "Show Format Badge",
//                subtitle = "TV, Movie, OVA etc",
//                checked = config.badges.showBadge,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            badges = config.badges.copy(
//                                showBadge = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            SettingToggleRow(
//                title = "Show Progress",
//                subtitle = "Episode progress indicator",
//                checked = config.badges.showProgress,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            badges = config.badges.copy(
//                                showProgress = it
//                            )
//                        )
//                    )
//
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* NSFW */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "NSFW Protection",
//            subtitle = "Sensitive content settings",
//            icon = Icons.Rounded.Security
//        ) {
//
//            SettingToggleRow(
//                title = "Blur NSFW Content",
//                subtitle = "Hide explicit media",
//                checked = config.nsfw.enabled,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            nsfw = config.nsfw.copy(
//                                enabled = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            AnimatedVisibility(config.nsfw.enabled) {
//
//                Column(
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//
//                    SettingSlider(
//                        title = "Blur Radius",
//                        value = config.nsfw.blurRadius,
//                        range = 0f..40f,
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    nsfw = config.nsfw.copy(
//                                        blurRadius = it
//                                    )
//                                )
//
//                            )
//                        }
//                    )
//
//                    SettingSlider(
//                        title = "Overlay Darkness",
//                        value = config.nsfw.dimAlpha,
//                        range = 0f..1f,
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    nsfw = config.nsfw.copy(
//                                        dimAlpha = it
//                                    )
//                                )
//
//                            )
//                        }
//                    )
//
//                    SettingToggleRow(
//                        title = "Show NSFW Label",
//                        subtitle = "Display warning text",
//                        checked = config.nsfw.showLabel,
//                        onCheckedChange = {
//                            onConfigChange(
//                                config.copy(
//                                    nsfw = config.nsfw.copy(
//                                        showLabel = it
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//
//                    SettingToggleRow(
//                        title = "Click To Reveal",
//                        subtitle = "Require interaction",
//                        checked = config.nsfw.clickToReveal,
//                        onCheckedChange = {
//                            onConfigChange(
//                                config.copy(
//                                    nsfw = config.nsfw.copy(
//                                        clickToReveal = it
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@OptIn(
//    ExperimentalMaterial3Api::class,
//    ExperimentalLayoutApi::class
//)
//@Composable
//private fun ListSettings(
//    displayPreference: MediaDisplayPreference,
//    onMediaDisplayPreferenceChange: (MediaDisplayPreference) -> Unit,
//) {
//    val onConfigChange: (ListConfig) -> Unit = {
//        onMediaDisplayPreferenceChange(
//            displayPreference.copy(
//                listConfig = it
//            )
//        )
//    }
//
//    val config = displayPreference.listConfig
//
//    val mode = MediaDisplayMode.List
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(20.dp),
//        verticalArrangement = Arrangement.spacedBy(18.dp)
//    ) {
//
//        /* ---------------------------------------------------------- */
//        /* VARIANT */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "List Style",
//            subtitle = "Choose the overall list appearance",
//            icon = Icons.Rounded.ViewAgenda
//        ) {
//
//            EnumChipRow(
//                current = displayPreference.listCardLayout,
//                options = listOf(
//                    MediaListCardVariant.COMPACT,
//                    MediaListCardVariant.COMFORTABLE,
//                    MediaListCardVariant.DETAILED
//                ),
//                label = {
//                    when (it) {
//                        MediaListCardVariant.COMPACT -> "Compact"
//                        MediaListCardVariant.COMFORTABLE -> "Comfortable"
//                        MediaListCardVariant.DETAILED -> "Detailed"
//                    }
//                },
//                onSelected = { variant ->
//                    onMediaDisplayPreferenceChange(
//                        displayPreference.copy(
//                            listCardLayout = variant
//                        )
//                    )
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* LAYOUT */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Layout",
//            subtitle = "Spacing, sizing and structure",
//            icon = Icons.Rounded.SpaceDashboard
//        ) {
//
//            SettingSlider(
//                title = "Item Spacing",
//                value = displayPreference.listSpacing.value,
//                range = 0f..32f,
//                suffix = "dp",
//                onValueChange = {
//                    onMediaDisplayPreferenceChange(
//                        displayPreference.copy(
//                            listSpacing = it.dp
//                        )
//                    )
//                }
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            SettingSlider(
//                title = "Poster Width",
//                value = config.ui.imageWidth.value,
//                range = 50f..180f,
//                suffix = "dp",
//                onValueChange = {
//                    onConfigChange(
//                        config.copy(
//                            ui = config.ui.copy(
//                                imageWidth = it.dp
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            SettingSlider(
//                title = "Corner Radius",
//                value = config.ui.cornerRadius.value,
//                range = 0f..36f,
//                suffix = "dp",
//                onValueChange = {
//                    onConfigChange(
//                        config.copy(
//                            ui = config.ui.copy(
//                                cornerRadius = it.dp
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            SettingSlider(
//                title = "Inner Content Spacing",
//                value = config.ui.spacing.value,
//                range = 4f..32f,
//                suffix = "dp",
//                onValueChange = {
//                    onConfigChange(
//                        config.copy(
//                            ui = config.ui.copy(
//                                spacing = it.dp
//                            )
//                        )
//                    )
//
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* INTERACTION */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Interaction",
//            subtitle = "Hover and animation behavior",
//            icon = Icons.Rounded.TouchApp
//        ) {
//
//            SettingToggleRow(
//                title = "Enable Hover Effects",
//                subtitle = "Animate cards on hover",
//                checked = config.interaction.enableHover,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            interaction = config.interaction.copy(
//                                enableHover = it
//                            )
//                        )
//
//                    )
//                }
//            )
//
//            AnimatedVisibility(config.interaction.enableHover) {
//
//                Column(
//                    verticalArrangement = Arrangement.spacedBy(18.dp)
//                ) {
//
//                    Spacer(Modifier.height(12.dp))
//
//                    SettingSlider(
//                        title = "Hover Scale",
//                        value = config.interaction.hoverScale,
//                        range = 1f..1.10f,
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    interaction = config.interaction.copy(
//                                        hoverScale = it
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//
//                    SettingSlider(
//                        title = "Pressed Scale",
//                        value = config.interaction.pressedScale,
//                        range = 0.85f..1f,
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    interaction = config.interaction.copy(
//                                        pressedScale = it
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//
//                    SettingSlider(
//                        title = "Hover Elevation",
//                        value = config.interaction.hoveredElevation.value,
//                        range = 0f..24f,
//                        suffix = "dp",
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    interaction = config.interaction.copy(
//                                        hoveredElevation = it.dp
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//
//                    SettingSlider(
//                        title = "Normal Elevation",
//                        value = config.interaction.normalElevation.value,
//                        range = 0f..12f,
//                        suffix = "dp",
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    interaction = config.interaction.copy(
//                                        normalElevation = it.dp
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//                }
//            }
//        }
//
//        /* ---------------------------------------------------------- */
//        /* CONTENT */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Content",
//            subtitle = "Control visible information",
//            icon = Icons.AutoMirrored.Rounded.Article
//        ) {
//
//            SettingToggleRow(
//                title = "Show Description",
//                subtitle = "Display synopsis text",
//                checked = config.content.showDescription,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            content = config.content.copy(
//                                showDescription = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            AnimatedVisibility(config.content.showDescription) {
//
//                Column {
//
//                    Spacer(Modifier.height(16.dp))
//
//                    SettingSlider(
//                        title = "Description Lines",
//                        value = config.content.descriptionMaxLines.toFloat(),
//                        range = 1f..8f,
//                        steps = 6,
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    content = config.content.copy(
//                                        descriptionMaxLines = it.toInt()
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            SettingToggleRow(
//                title = "Show Genres",
//                subtitle = "Display media genres",
//                checked = config.content.showGenres,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            content = config.content.copy(
//                                showGenres = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            SettingToggleRow(
//                title = "Show Score",
//                subtitle = "Display ratings",
//                checked = config.content.showScore,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            content = config.content.copy(
//                                showScore = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            SettingToggleRow(
//                title = "Show Progress",
//                subtitle = "Display watch/read progress",
//                checked = config.content.showProgress,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            content = config.content.copy(
//                                showProgress = it
//                            )
//                        )
//                    )
//
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* IMAGE */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "Poster Image",
//            subtitle = "Poster scaling and appearance",
//            icon = Icons.Rounded.Image
//        ) {
//
//            SettingSlider(
//                title = "Poster Corner Radius",
//                value = config.image.cornerRadius.value,
//                range = 0f..32f,
//                suffix = "dp",
//                onValueChange = {
//                    onConfigChange(
//                        config.copy(
//                            image = config.image.copy(
//                                cornerRadius = it.dp
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            SettingSlider(
//                title = "Hover Zoom Scale",
//                value = config.image.hoverZoomScale,
//                range = 1f..1.20f,
//                onValueChange = {
//                    onConfigChange(
//                        config.copy(
//                            image = config.image.copy(
//                                hoverZoomScale = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            Spacer(Modifier.height(18.dp))
//
//            SettingToggleRow(
//                title = "Enable Hover Zoom",
//                subtitle = "Zoom poster on hover",
//                checked = config.image.enableHoverZoom,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            image = config.image.copy(
//                                enableHoverZoom = it
//                            )
//                        )
//                    )
//
//                }
//            )
//        }
//
//        /* ---------------------------------------------------------- */
//        /* NSFW */
//        /* ---------------------------------------------------------- */
//
//        SettingsCard(
//            title = "NSFW Protection",
//            subtitle = "Sensitive content visibility",
//            icon = Icons.Rounded.Security
//        ) {
//
//            SettingToggleRow(
//                title = "Blur NSFW Content",
//                subtitle = "Hide explicit media",
//                checked = config.nsfw.enabled,
//                onCheckedChange = {
//                    onConfigChange(
//                        config.copy(
//                            nsfw = config.nsfw.copy(
//                                enabled = it
//                            )
//                        )
//                    )
//
//                }
//            )
//
//            AnimatedVisibility(config.nsfw.enabled) {
//
//                Column(
//                    verticalArrangement = Arrangement.spacedBy(18.dp)
//                ) {
//
//                    Spacer(Modifier.height(12.dp))
//
//                    SettingSlider(
//                        title = "Blur Radius",
//                        value = config.nsfw.blurRadius,
//                        range = 0f..40f,
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    nsfw = config.nsfw.copy(
//                                        blurRadius = it
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//
//                    SettingSlider(
//                        title = "Overlay Darkness",
//                        value = config.nsfw.dimAlpha,
//                        range = 0f..1f,
//                        onValueChange = {
//                            onConfigChange(
//                                config.copy(
//                                    nsfw = config.nsfw.copy(
//                                        dimAlpha = it
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//
//                    SettingToggleRow(
//                        title = "Show NSFW Label",
//                        subtitle = "Display warning badge",
//                        checked = config.nsfw.showLabel,
//                        onCheckedChange = {
//                            onConfigChange(
//                                config.copy(
//                                    nsfw = config.nsfw.copy(
//                                        showLabel = it
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//
//                    SettingToggleRow(
//                        title = "Click To Reveal",
//                        subtitle = "Require interaction to reveal",
//                        checked = config.nsfw.clickToReveal,
//                        onCheckedChange = {
//                            onConfigChange(
//                                config.copy(
//                                    nsfw = config.nsfw.copy(
//                                        clickToReveal = it
//                                    )
//                                )
//                            )
//
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//// --- Helper Composables ---
//
//@Composable
//fun SettingsCard(
//    title: String,
//    subtitle: String? = null,
//    icon: ImageVector,
//    content: @Composable ColumnScope.() -> Unit
//) {
//    ElevatedCard(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(24.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(20.dp),
//            verticalArrangement = Arrangement.spacedBy(20.dp)
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Surface(
//                    shape = CircleShape,
//                    color = MaterialTheme.colorScheme.secondaryContainer
//                ) {
//                    Icon(
//                        imageVector = icon,
//                        contentDescription = null,
//                        modifier = Modifier.padding(10.dp)
//                    )
//                }
//
//                Column {
//                    Text(
//                        title,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.SemiBold
//                    )
//
//                    subtitle?.let {
//                        Text(
//                            it,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//
//            content()
//        }
//    }
//}
//
//
//@Composable
//private fun SettingToggleRow(
//    title: String,
//    subtitle: String,
//    checked: Boolean,
//    onCheckedChange: (Boolean) -> Unit
//) {
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = FontWeight.Medium
//            )
//
//            Text(
//                text = subtitle,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//
//        Switch(
//            checked = checked,
//            onCheckedChange = onCheckedChange
//        )
//    }
//}
//
//@Composable
//private fun SettingSlider(
//    title: String,
//    value: Float,
//    range: ClosedFloatingPointRange<Float>,
//    steps: Int = 0,
//    suffix: String = "",
//    onValueChange: (Float) -> Unit
//) {
//    var sliderValue by remember(value) {
//        mutableFloatStateOf(value)
//    }
//
//
//    Column {
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyLarge
//            )
//
//            Text(
//                text = "${value.toInt()}$suffix",
//                color = MaterialTheme.colorScheme.primary,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//
//        Slider(
//            value = sliderValue,
//            onValueChange = {
//                sliderValue = it
//            },
//            onValueChangeFinished = {
//                onValueChange(sliderValue)
//            },
//            valueRange = range,
//            steps = steps
//        )
//    }
//}
//
//@Composable
//private fun SettingSectionTitle(
//    title: String
//) {
//
//    Text(
//        text = title,
//        style = MaterialTheme.typography.labelLarge,
//        color = MaterialTheme.colorScheme.primary,
//        fontWeight = FontWeight.SemiBold
//    )
//}
//
//@Composable
//private fun <T> EnumChipRow(
//    current: T,
//    options: List<T>,
//    label: (T) -> String,
//    onSelected: (T) -> Unit
//) {
//
//    FlowRow(
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//
//        options.forEach { option ->
//
//            FilterChip(
//                selected = current == option,
//                onClick = { onSelected(option) },
//                label = {
//                    Text(label(option))
//                }
//            )
//        }
//    }
//}
//
//@Composable
//private fun PresetSelector(
//    selected: PosterLayout,
//    onSelected: (PosterLayout) -> Unit
//) {
//
//    EnumChipRow(
//        current = selected,
//        options = listOf(
//            PosterLayout.Modern,
//            PosterLayout.Overlay,
//            PosterLayout.Minimal,
//            PosterLayout.Compact
//        ),
//        label = {
//            when (it) {
//                PosterLayout.Modern -> "Modern"
//                PosterLayout.Overlay -> "Cinematic"
//                PosterLayout.Minimal -> "Minimal"
//                PosterLayout.Compact -> "Compact"
//            }
//        },
//        onSelected = onSelected
//    )
//}
//
//
//@Composable
//private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
//        content()
//    }
//}
//
//@Composable
//private fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(text = label, style = MaterialTheme.typography.bodyMedium)
//        Switch(checked = checked, onCheckedChange = onCheckedChange)
//    }
//}
//
//
//@Preview
//@Composable
//private fun DisplaySettingPanelPreview() {
//    DisplaySettingsPanel(
//        displayPreferences = MediaDisplayPreference(),
//        onMediaDisplayPreferenceChange = { },
//        modifier = Modifier,
//        contentPadding = PaddingValues(0.dp)
//    )
//}