package com.ghost.zeku.presentation.components.media.list


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.zeku.domain.model.ProviderType
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.OnMediaAction
import com.ghost.zeku.presentation.theme.AppTheme

@Composable
fun MediaListCard(
    modifier: Modifier = Modifier,
    data: MediaListUiData,
    selected: Boolean = false,
    layout: ListCardLayout = ListCardLayout.Modern,
    config: ListCardConfig = ListCardDefaults.forLayout(layout),
    onAction: OnMediaAction,
) {

    val interactionSource = remember { MutableInteractionSource() }

    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {

            isPressed && config.enablePress ->
                config.scaleOnPress

            isHovered && config.enableHover ->
                config.scaleOnHover

            selected ->
                1.01f

            else -> 1f
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessLow
        ),
        label = "list_card_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {

            selected -> MaterialTheme.colorScheme.primary

            isHovered -> MaterialTheme.colorScheme.outline.copy(
                alpha = 0.4f
            )

            else -> Color.Transparent
        },
        label = "list_card_border"
    )

    val containerColor by animateColorAsState(
        targetValue = when {

            selected -> MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.18f
            )

            isHovered -> MaterialTheme.colorScheme.surfaceContainerHigh

            else -> MaterialTheme.colorScheme.surface
        },
        label = "list_card_container"
    )

    val elevation by animateDpAsState(
        targetValue = when {

            selected -> config.elevation + 6.dp

            isHovered -> config.elevation + 2.dp

            else -> config.elevation
        },
        label = "list_card_elevation"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,

                onClick = {
                    onAction(
                        MediaAction.MediaClick(
                            data.id,
                            data.mediaType
                        )
                    )
                },

                onLongClick = {
                    onAction(
                        MediaAction.LongClick(
                            data.id,
                            data.mediaType
                        )
                    )
                }
            ),

        shape = RoundedCornerShape(
            config.cornerRadius
        ),

        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),

        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = borderColor
        ),

        elevation = if (config.enableShadow) {
            CardDefaults.cardElevation(
                defaultElevation = elevation
            )
        } else {
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        }
    ) {

        Box {

            when (layout) {

                ListCardLayout.Minimal -> {
                    MinimalListCard(
                        data,
                        config,
                        onAction
                    )
                }

                ListCardLayout.Compact -> {
                    CompactListCard(
                        data,
                        config,
                        onAction
                    )
                }

                ListCardLayout.Modern -> {
                    ModernListCard(
                        data,
                        config,
                        onAction
                    )
                }

                ListCardLayout.Detailed -> {
                    DetailedListCard(
                        data,
                        config,
                        onAction
                    )
                }
            }


            Row {
                AnimatedVisibility(
                    visible = selected,
                    modifier = Modifier
                ) {

                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(4.dp)
                            .fillMaxHeight(0.7f)
                            .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                            .background(
                                MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        }
    }
}


/**
 * Minimal list card: image + title (optional subtitle) + trailing icon.
 */
@Composable
private fun MinimalListCard(
    data: MediaListUiData,
    config: ListCardConfig,
    onAction: OnMediaAction
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster image
        MediaAsyncImage(
            url = data.getDisplayImageUrl(),
            contentDescription = data.title,
            modifier = Modifier
                .width(config.imageWidth)
                .aspectRatio(config.aspectRatio)
                .clip(RoundedCornerShape(config.cornerRadius)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = config.maxTitleLines,
                overflow = TextOverflow.Ellipsis
            )

            if (config.showSubtitle && !data.subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = config.maxSubtitleLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Trailing action icon
        if (config.showTrailingIcon) {
            IconButton(
                onClick = { onAction(MediaAction.TrailingClick(data.id, data.mediaType)) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Action",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/**
 * Compact list card: adds progress indicator and score badge to the Minimal variant.
 */
@Composable
private fun CompactListCard(
    data: MediaListUiData,
    config: ListCardConfig,
    onAction: OnMediaAction
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster image
        MediaAsyncImage(
            url = data.getDisplayImageUrl(),
            contentDescription = data.title,
            modifier = Modifier
                .width(config.imageWidth)
                .aspectRatio(config.aspectRatio)
                .clip(RoundedCornerShape(config.cornerRadius)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text content + extras
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = config.maxTitleLines,
                overflow = TextOverflow.Ellipsis
            )

            if (config.showSubtitle && !data.subtitle.isNullOrBlank()) {
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = config.maxSubtitleLines,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress bar
            if (config.showProgress && data.progress != null) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { data.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            // Score badge
            if (config.showScore && data.score != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Score",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", data.score),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Trailing action icon
        if (config.showTrailingIcon) {
            IconButton(
                onClick = { onAction(MediaAction.TrailingClick(data.id, data.mediaType)) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Action",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/**
 * Modern list card: uses a gradient background, genre chips, and a prominent score bubble.
 */
@Composable
private fun ModernListCard(
    data: MediaListUiData,
    config: ListCardConfig,
    onAction: OnMediaAction
) {
    val shape = RoundedCornerShape(config.cornerRadius)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster image
        MediaAsyncImage(
            url = data.getDisplayImageUrl(),
            contentDescription = data.title,
            modifier = Modifier
                .width(config.imageWidth)
                .aspectRatio(config.aspectRatio)
                .clip(shape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = config.maxTitleLines,
                overflow = TextOverflow.Ellipsis
            )

            if (config.showSubtitle && !data.subtitle.isNullOrBlank()) {
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = config.maxSubtitleLines,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Genres row
            if (config.showGenres && !data.genres.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    data.genres.take(3).forEach { genre ->
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            // Progress bar
            if (config.showProgress && data.progress != null) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { data.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        // Score bubble
        if (config.showScore && data.score != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = String.format("%.1f", data.score),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Trailing action icon
        if (config.showTrailingIcon) {
            IconButton(
                onClick = { onAction(MediaAction.TrailingClick(data.id, data.mediaType)) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Action",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/**
 * Detailed list card: shows all available details – description, progress, score, genres, etc.
 */
@Composable
private fun DetailedListCard(
    data: MediaListUiData,
    config: ListCardConfig,
    onAction: OnMediaAction
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Poster image
        MediaAsyncImage(
            url = data.getDisplayImageUrl(),
            contentDescription = data.title,
            modifier = Modifier
                .width(config.imageWidth)
                .aspectRatio(config.aspectRatio)
                .clip(RoundedCornerShape(config.cornerRadius)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = config.maxTitleLines,
                overflow = TextOverflow.Ellipsis
            )

            if (config.showSubtitle && !data.subtitle.isNullOrBlank()) {
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = config.maxSubtitleLines,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Score + genres row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (config.showScore && data.score != null) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = String.format("%.1f", data.score),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (config.showGenres && !data.genres.isNullOrEmpty()) {
                    Text(
                        text = data.genres.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Description
            if (config.showDescription && !data.description.isNullOrBlank()) {
                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = config.maxDescriptionLines,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Progress bar
            if (config.showProgress && data.progress != null) {
                LinearProgressIndicator(
                    progress = { data.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        // Trailing action icon (always top-aligned due to parent's Alignment.Top)
        if (config.showTrailingIcon) {
            IconButton(
                onClick = { onAction(MediaAction.TrailingClick(data.id, data.mediaType)) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Action",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewListCard() {
    val sampleMediaList = listOf(
        MediaListUiData(
            id = 1,
            title = "Neon Skies",
            subtitle = "Season 1",
            description = "A hacker-turned-reluctant-hero navigates a neon-drenched metropolis to expose corporate secrets.",
            imageUrl = "https://example.com/images/neon_skies.jpg",
            bannerImageUrl = "https://example.com/banners/neon_skies_banner.jpg",
            mediaType = MediaType.ANIME,
            progress = 0.45f,
            score = 8.3f,
            genres = listOf("Sci-fi", "Thriller"),
            isWatched = false,
            currentEpisode = 5,
            totalEpisodes = 12,
            releaseYear = 2024,
            ageRating = "TV-14",
            isAdult = false,
            isHidden = false,
            tags = listOf("cyberpunk", "antihero")
        ),
        MediaListUiData(
            id = 2,
            title = "Last Light",
            subtitle = null,
            description = "A tense survival drama about a small-town community facing a mysterious blackout.",
            imageUrl = "https://example.com/images/last_light.jpg",
            bannerImageUrl = null,
            mediaType = MediaType.ANIME,
            progress = 1.0f,
            score = 7.1f,
            genres = listOf("Drama", "Mystery"),
            isWatched = true,
            currentEpisode = null,
            totalEpisodes = null,
            releaseYear = 2022,
            ageRating = "R",
            isAdult = false,
            isHidden = false,
            tags = listOf("survival", "ensemble")
        ),
        MediaListUiData(
            id = 3,
            title = "Garden of Echoes",
            subtitle = "Limited Series",
            description = "An intimate, surreal exploration of memory and forgiveness across three timelines.",
            imageUrl = "https://example.com/images/garden_of_echoes.jpg",
            bannerImageUrl = "https://example.com/banners/garden_of_echoes_banner.jpg",
            mediaType = MediaType.ANIME,
            progress = 0.0f,
            score = 8.9f,
            genres = listOf("Drama", "Fantasy"),
            isWatched = false,
            currentEpisode = 0,
            totalEpisodes = 6,
            releaseYear = 2025,
            ageRating = "TV-MA",
            isAdult = false,
            isHidden = false,
            tags = listOf("arthouse", "mind-bending")
        ),
        MediaListUiData(
            id = 4,
            title = "Circuit Breaker",
            subtitle = null,
            description = "A fast-paced documentary about the rise of renewable energy startups.",
            imageUrl = "https://example.com/images/circuit_breaker.jpg",
            bannerImageUrl = null,
            mediaType = MediaType.ANIME,
            progress = 0.75f,
            score = 7.8f,
            genres = listOf("Documentary", "Science"),
            isWatched = false,
            currentEpisode = null,
            totalEpisodes = null,
            releaseYear = 2021,
            ageRating = "PG",
            isAdult = false,
            isHidden = false,
            tags = listOf("energy", "innovation")
        ),
        MediaListUiData(
            id = 5,
            title = "Midnight Market",
            subtitle = "Season 2",
            description = "A whimsical ensemble comedy set in a bustling night market where secrets and romances bloom.",
            imageUrl = "https://example.com/images/midnight_market.jpg",
            bannerImageUrl = "https://example.com/banners/midnight_market_banner.jpg",
            mediaType = MediaType.ANIME,
            progress = 0.12f,
            score = 6.6f,
            genres = listOf("Comedy", "Romance"),
            isWatched = false,
            currentEpisode = 2,
            totalEpisodes = 10,
            releaseYear = 2023,
            ageRating = "TV-14",
            isAdult = false,
            isHidden = false,
            tags = listOf("ensemble", "romcom")
        )
    )

    AppTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            sampleMediaList.forEach { item ->
                val layout = ListCardLayout.entries.random()
                Text(text = layout.name, style = TextStyle(fontSize = 20.sp))
                MediaListCard(
                    modifier = Modifier,
                    data = item,
                    layout = layout,
                    config = ListCardDefaults.forLayout(layout),
                    onAction = { TODO() },
                )
                HorizontalDivider()
            }
        }
    }
}