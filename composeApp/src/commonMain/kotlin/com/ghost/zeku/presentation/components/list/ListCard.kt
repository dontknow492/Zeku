package com.ghost.zeku.presentation.components.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.zeku.presentation.common.DeviceType
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration

@Composable
fun MediaListCard(
    data: MediaListUiData,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val isDesktop = rememberPlatformConfiguration().type is DeviceType.Desktop

    val elevation by animateFloatAsState(
        targetValue = if (isHovered) 8f else 2f,
        animationSpec = tween(200),
        label = "cardElevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation.dp, shape = RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(data.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isDesktop) 20.dp else 16.dp,
                    vertical = if (isDesktop) 16.dp else 12.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ---- Cover Image with Airing Badge ----
            Box(
                modifier = Modifier
                    .width(if (isDesktop) 100.dp else 80.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                MediaAsyncImage(
                    url = data.coverImageUrl,
                    contentDescription = data.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Airing indicator (top‑right)
                if (data.isAiring) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // ---- Content Column ----
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title + Score Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Score (circular or chip style)
                    data.score?.let { score ->
                        ScoreChip(score = score)
                    }
                }

                // Genres as Chips
                if (data.genres.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        data.genres.take(3).forEach { genre ->
                            AssistChip(
                                onClick = { /* optional filter */ },
                                label = { Text(genre, fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }

                // Metadata row (format, year, status)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.subTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (data.status != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = data.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Progress section (bar + text)
                if (data.progress != null || data.progressText != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (data.progressText != null) {
                            Text(
                                text = data.progressText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        if (data.progress != null) {
                            LinearProgressIndicator(
                                progress = { data.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Score Chip – Modern circular indicator with star or number
// ----------------------------------------------------------------------------
@Composable
private fun ScoreChip(score: Float) {
    val scoreInt = (score * 10).toInt() / 10f // 1 decimal
    val color = when {
        score >= 8.0 -> Color(0xFF4CAF50) // Green
        score >= 6.0 -> Color(0xFFFFA000) // Amber
        else -> MaterialTheme.colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Score",
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "%.1f".format(scoreInt),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
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
                MediaListCard(sampleData, onClick = {})
                MediaListCard(
                    sampleData.copy(
                        title = "Solo Leveling",
                        score = 7.5f,
                        status = "Releasing",
                        isAiring = true,
                        progress = 0.66f,
                        progressText = "8 / 12 EPs"
                    ),
                    onClick = {}
                )
                MediaListCard(
                    sampleData.copy(
                        title = "Some Isekai Anime That Has a Very Long Title That Wraps",
                        score = 4.2f,
                        status = "Publishing",
                        progress = null,
                        progressText = null
                    ),
                    onClick = {}
                )
            }
        }
    }
}