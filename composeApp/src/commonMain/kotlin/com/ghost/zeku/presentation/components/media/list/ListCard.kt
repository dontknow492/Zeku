package com.ghost.zeku.presentation.components.media.list

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.presentation.common.MediaImage
import com.ghost.zeku.presentation.common.chips.GenreChip
import com.ghost.zeku.presentation.common.chips.ScoreChip
import com.ghost.zeku.presentation.common.chips.StatusChip
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.OnListAction


@Composable
fun MediaListCard(
    data: MediaListUiData,
    onAction: OnListAction,
    modifier: Modifier = Modifier,
    variant: MediaListCardVariant = MediaListCardVariant.COMFORTABLE,
    config: MediaListCardConfig = MediaListDefaults.config(variant)
) {
    when (variant) {
        MediaListCardVariant.COMPACT -> CompactMediaListCard(data, onAction, modifier, config)
        MediaListCardVariant.COMFORTABLE -> ComfortableMediaListCard(data, onAction, modifier, config)
        MediaListCardVariant.DETAILED -> DetailedMediaListCard(data, onAction, modifier, config)
    }
}


@Composable
private fun CompactMediaListCard(
    data: MediaListUiData,
    onAction: OnListAction,
    modifier: Modifier,
    config: MediaListCardConfig
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAction(MediaAction.MediaClick(data.id)) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaListImage(data, onAction, config)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = data.subTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        data.score?.let {
            Text(
                text = "%.1f".format(it),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
private fun ComfortableMediaListCard(
    data: MediaListUiData,
    onAction: OnListAction,
    modifier: Modifier,
    config: MediaListCardConfig
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val elevation by animateDpAsState(
        if (isHovered) config.interaction.hoveredElevation else config.interaction.normalElevation
    )


    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = config.ui.maxWidth)
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onAction(MediaAction.MediaClick(data.id)) },
            shape = RoundedCornerShape(config.ui.cornerRadius),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(config.ui.spacing),
                horizontalArrangement = Arrangement.spacedBy(config.ui.spacing)
            ) {
                MediaListImage(data, onAction, config)
                MediaListContent(data, config)
            }
        }
    }
}

@Composable
private fun DetailedMediaListCard(
    data: MediaListUiData,
    onAction: OnListAction,
    modifier: Modifier,
    config: MediaListCardConfig
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = config.ui.maxWidth)
                .fillMaxWidth()
                .clickable { onAction(MediaAction.MediaClick(data.id)) },
            shape = RoundedCornerShape(config.ui.cornerRadius)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Bigger image
                MediaListImage(data, onAction, config)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // Title + Score
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = data.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        data.score?.let { ScoreChip(it) }
                    }

                    // Subtitle
                    Text(
                        text = data.subTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Genres (more space)
                    if (data.genres.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            data.genres.take(5).forEach {
                                GenreChip(it)
                            }
                        }
                    }

                    // Status
                    data.status?.let {
                        StatusChip(it)
                    }

                    // Progress
                    data.progress?.let {
                        LinearProgressIndicator(
                            progress = { it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    data.progressText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaListImage(
    data: MediaListUiData,
    onAction: OnListAction,
    config: MediaListCardConfig
) {
    Box(
        modifier = Modifier
            .width(config.ui.imageWidth)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        MediaImage(
            imageUrl = data.coverImageUrl,
            title = data.title,
            isNsfw = data.isNsfw,
            isRevealed = data.isNsfwRevealed,
            onReveal = {
                onAction(MediaAction.RevealNsfw(data.id))
            },
            nsfwConfig = config.nsfw,
            mediaImageConfig = config.image,
            badge = null
        )

        if (data.isAiring) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            )
        }
    }
}


@Composable
private fun MediaListContent(
    data: MediaListUiData,
    config: MediaListCardConfig
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // TITLE + SCORE
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.title,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (config.content.showScore && data.score != null) {
                ScoreChip(data.score)
            }
        }

        // GENRES
        if (config.content.showGenres && data.genres.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                data.genres.take(3).forEach {
                    GenreChip(it)
                }
            }
        }

        // META
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = data.subTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            data.status?.let {
                StatusChip(it)
            }
        }

        // Description
        if (config.content.showDescription && data.description != null) {
            Text(
                text = data.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = config.content.descriptionMaxLines,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // PROGRESS
        if (config.content.showProgress && (data.progress != null || data.progressText != null)) {
            Column {
                data.progressText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                data.progress?.let {
                    LinearProgressIndicator(
                        progress = { it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}


// ----------------------------------------------------------------------------
// Preview (shows both desktop & mobile)
// ----------------------------------------------------------------------------
@Preview(name = "Mobile", widthDp = 400, heightDp = 800)
@Preview(name = "Desktop", widthDp = 900, heightDp = 600)
@Composable
private fun MediaListCardPreview() {
    val sampleData = MediaListUiData(
        id = 1,
        description = "An elf mage begins a journey to understand humanity after the death of her long-time companions." +
                " A slow, emotional, and beautifully written story about time, memory, and connection.",
        title = "Frieren: Beyond Journey's End",
        coverImageUrl = "",
        subTitle = "TV • 2023",
        genres = listOf("Adventure", "Drama", "Fantasy"),
        status = "Finished",
        score = 9.4f,
        progress = 0.75f,
        progressText = "22 / 28 EPs",
        isAiring = false
    )

    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MediaListCard(sampleData, onAction = {})
                MediaListCard(
                    sampleData.copy(
                        title = "Solo Leveling",
                        score = 7.5f,
                        status = "Releasing",
                        isAiring = true,
                        progress = 0.66f,
                        progressText = "8 / 12 EPs"
                    ),
                    onAction = {}
                )
                MediaListCard(
                    sampleData.copy(
                        title = "Some Isekai Anime That Has a Very Long Title That Wraps",
                        score = 4.2f,
                        status = "Publishing",
                        progress = null,
                        progressText = null
                    ),
                    onAction = {}
                )
            }
        }
    }
}