package com.ghost.zeku.presentation.components.media.review

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ghost.zeku.domain.model.media.Review
import com.ghost.zeku.presentation.common.chips.ScoreChip
import com.ghost.zeku.presentation.components.media.ReviewAction
import com.ghost.zeku.utils.formatTimestamp
import kotlin.math.absoluteValue

@Composable
fun ReviewCard(
    review: Review,
    config: ReviewCardConfig = ReviewCardConfig(),
    modifier: Modifier = Modifier,
    onAction: (ReviewAction) -> Unit = {}
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = config.animationSpec
    )

    var expanded by remember { mutableStateOf(false) }

    val shape = config.shape

    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            // 🌈 Glass gradient background
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                )
            )
            // ✨ Soft border
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape
            )
            // 🌟 Subtle glow
            .drawBehind {
                drawRoundRect(
                    color = colorScheme.primary.copy(alpha = 0.05f),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onAction(ReviewAction.Click(review))
            }
            .padding(config.padding)
            .animateContentSize() // 🔥 smooth expand
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // =========================
            // HEADER
            // =========================
            Row(verticalAlignment = Alignment.CenterVertically) {

                if (config.showAvatar) {
                    ReviewAvatar(
                        name = review.author,
                        avatarUrl = review.authorAvatar
                    )
                    Spacer(Modifier.width(10.dp))
                }

                Column(Modifier.weight(1f)) {

                    Text(
                        text = review.author.ifBlank { "Unknown" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (config.showDate && review.createdAt != null) {
                        Text(
                            text = formatTimestamp(review.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                if (config.showScore && review.score != null) {
                    ScoreChip(review.score.toFloat())
                }
            }

            // =========================
            // SUMMARY
            // =========================
            if (config.showSummary && !review.summary.isNullOrBlank()) {
                Text(
                    text = review.summary,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // =========================
            // BODY + SPOILER
            // =========================
            Box {

                val isSpoilerHidden = review.isSpoiler && !expanded

                Text(
                    text = review.body.ifBlank { "No content available." },
                    maxLines = if (expanded) Int.MAX_VALUE else config.maxCollapsedLines,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )

                if (isSpoilerHidden) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            )
                            .clickable { expanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Spoiler — Tap to reveal",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // =========================
            // DIVIDER
            // =========================
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // =========================
            // FOOTER
            // =========================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    if (config.showUpvotes) {
                        Text(
                            text = "👍 ${review.upvotes}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (review.isSpoiler) {
                        Text(
                            text = "Spoiler",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (config.enableExpand) {
                    Text(
                        text = if (expanded) "Less" else "More",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            expanded = !expanded
                            onAction(ReviewAction.Expand(review))
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun ReviewAvatar(
    name: String,
    avatarUrl: String?,
    size: Dp = 40.dp
) {
    val initials = remember(name) {
        name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifBlank { "?" }
    }

    val bgColor = remember(name) {
        val colors = listOf(
            Color(0xFFEF5350),
            Color(0xFFAB47BC),
            Color(0xFF5C6BC0),
            Color(0xFF29B6F6),
            Color(0xFF66BB6A),
            Color(0xFFFFCA28),
        )
        colors[name.hashCode().absoluteValue % colors.size]
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {

        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewReviewCard() {

    val sample = Review(
        id = 1,
        author = "Ghost",
        authorAvatar = null,
        score = 85,
        summary = "Really solid anime",
        body = "This anime delivers strong characters, pacing, and animation. Definitely worth watching.",
        upvotes = 120,
        isSpoiler = false,
        createdAt = System.currentTimeMillis()
    )

    MaterialTheme {
        ReviewCard(
            review = sample,
            modifier = Modifier.padding(16.dp)
        )
    }
}

