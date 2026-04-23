package com.ghost.zeku.presentation.components.hero

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
import com.ghost.zeku.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.*
import kotlin.math.max

// ============================================================================
// MAIN HERO BANNER – Responsive, cinematic, theme‑aware
// ============================================================================

@Composable
fun MediaHeroBanner(
    data: MediaHeroUiData,
    onWatchClick: (MediaHeroUiData) -> Unit,
    onDetailsClick: (MediaHeroUiData) -> Unit,
    modifier: Modifier = Modifier,
    config: HeroConfig? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val platform = rememberPlatformConfiguration()
    val isDesktop = platform.screenSizeDp.width > 600.dp

    val baseConfig = HeroDefaults.config(isDesktop)
    val resolvedConfig = config ?: baseConfig

    val bannerAspect = if (isDesktop)
        resolvedConfig.aspectRatioDesktop
    else
        resolvedConfig.aspectRatioMobile

    // Smooth, slow zoom effect on hover/press (cinematic)
    val imageScale by animateFloatAsState(
        targetValue = when {
            isPressed -> resolvedConfig.pressedScale
            isHovered && resolvedConfig.enableHoverZoom -> resolvedConfig.hoveredScale
            else -> 1f
        },
        animationSpec = tween(1200),
        label = "HeroZoom"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(resolvedConfig.cornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onDetailsClick(data) }
            )
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(bannerAspect)
                .fillMaxWidth()
        ) {
            // 1. Background image with zoom
            MediaAsyncImage(
                url = if (isDesktop) data.bannerImageUrl else data.coverImageUrl,
                contentDescription = data.title,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(imageScale)
            )

            // 2. Dynamic scrim (layered gradients)
            HeroScrim(
                isDesktop = isDesktop,
                intensity = resolvedConfig.scrimIntensity,
            )

            // 3. Content overlay
            HeroContent(
                data = data,
                isDesktop = isDesktop,
                resolvedConfig = resolvedConfig,
                onWatchClick = onWatchClick,
                onDetailsClick = onDetailsClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ----------------------------------------------------------------------------
// RESPONSIVE SCRIM – Uses theme colors, not hardcoded black/white
// ----------------------------------------------------------------------------
@Composable
private fun HeroScrim(
    isDesktop: Boolean,
    intensity: Float
) {
    val scrim = MaterialTheme.colorScheme.scrim

    // Left‑to‑right gradient (desktop only) – improves text readability on wide images
    if (isDesktop) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(scrim.copy(alpha = intensity - 0.05f), Color.Transparent),
                        startX = 0f,
                        endX = 1200f
                    )
                )
        )
    }

    // Bottom‑to‑top gradient (always present, stronger on mobile)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, scrim.copy(alpha = intensity + 0.05f)),
                    startY = if (isDesktop) 0.4f else 0.2f // earlier fade on mobile
                )
            )
    )
}

// ----------------------------------------------------------------------------
// CONTENT LAYOUT – Buttons, title, genres, description
// ----------------------------------------------------------------------------
@Composable
private fun HeroContent(
    data: MediaHeroUiData,
    isDesktop: Boolean,
    resolvedConfig: HeroConfig,
    onWatchClick: (MediaHeroUiData) -> Unit,
    onDetailsClick: (MediaHeroUiData) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier.padding(
            if (isDesktop) resolvedConfig.contentPaddingDesktop
            else resolvedConfig.contentPaddingMobile
        ),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = if (isDesktop) Alignment.Start else Alignment.CenterHorizontally
    ) {
        // Badge (e.g., "TRENDING", "NEW EPISODE")
        AnimatedVisibility(resolvedConfig.showBadge) {
            data.badgeText?.let {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = it.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }


        // Title
        Text(
            text = data.title,
            style = if (isDesktop) MaterialTheme.typography.displayLarge else MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary, // high contrast over scrim
            fontWeight = FontWeight.Bold,
            maxLines = resolvedConfig.titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Genres row
        AnimatedVisibility(resolvedConfig.showGenres) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                data.genres.take(max(4, data.genres.size)).forEachIndexed { index, genre ->
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                    if (index < data.genres.lastIndex) {
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }


        // Description (only on desktop, keeps mobile clean)
        AnimatedVisibility((isDesktop || resolvedConfig.showDescriptionOnMobile) && data.description.isNotBlank()) {
            Text(
                text = data.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .padding(bottom = 24.dp)
            )
        }

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = if (!isDesktop) Modifier.fillMaxWidth() else Modifier
        ) {
            // Watch Now button (primary)
            Button(
                onClick = { onWatchClick(data) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = if (!isDesktop) Modifier.weight(1f) else Modifier
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(Res.string.hero_watch_content_desc)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.hero_watch_now),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Details button (outlined with subtle border)
            OutlinedButton(
                onClick = { onDetailsClick(data) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(32.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = if (!isDesktop) Modifier.weight(1f) else Modifier
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(Res.string.hero_details_content_desc)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.hero_details),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// PREVIEWS (Desktop & Mobile)
// ----------------------------------------------------------------------------
@Preview(showBackground = true, widthDp = 900, heightDp = 500)
@Composable
private fun HeroBannerDesktopPreview() {
    val mockData = MediaHeroUiData(
        id = 1,
        title = "Solo Leveling",
        bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/151807-ngjsN8vJ8p83.jpg",
        coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx151807-m1gX3iqITmI6.png",
        description = "In a world where hunters must battle deadly monsters, a notoriously weak hunter named Sung Jinwoo finds himself in a struggle for survival.",
        genres = listOf("Action", "Adventure", "Fantasy"),
        badgeText = "Releasing"
    )

    AppTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            MediaHeroBanner(
                data = mockData,
                onWatchClick = {},
                onDetailsClick = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun HeroBannerMobilePreview() {
    val mockData = MediaHeroUiData(
        id = 1,
        title = "Solo Leveling",
        bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/151807-ngjsN8vJ8p83.jpg",
        coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx151807-m1gX3iqITmI6.png",
        description = "Hidden on mobile",
        genres = listOf("Action", "Adventure", "Fantasy"),
        badgeText = "Releasing"
    )

    AppTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            MediaHeroBanner(
                data = mockData,
                onWatchClick = {},
                onDetailsClick = {}
            )
        }
    }
}