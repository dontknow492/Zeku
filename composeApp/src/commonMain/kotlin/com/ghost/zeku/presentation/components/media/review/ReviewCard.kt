package com.ghost.zeku.presentation.components.media.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.media.Review
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.common.chips.ScoreChip
import com.ghost.zeku.presentation.components.media.ReviewAction
import com.ghost.zeku.utils.formatTimestamp

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

    Card(
        onClick = { onAction(ReviewAction.Click(review)) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier.padding(config.padding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // -------------------------
                // HEADER
                // -------------------------
                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (config.showAvatar && review.authorAvatar != null) {
                        MediaAsyncImage(
                            url = review.authorAvatar,
                            contentDescription = review.author,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )

                        Spacer(Modifier.width(8.dp))
                    }

                    Column(Modifier.weight(1f)) {

                        Text(
                            text = review.author,
                            style = MaterialTheme.typography.titleSmall
                        )

                        if (config.showDate && review.createdAt != null) {
                            Text(
                                text = formatTimestamp(review.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (config.showScore && review.score != null) {
                        ScoreChip(review.score.toFloat())
                    }
                }

                // -------------------------
                // SUMMARY
                // -------------------------
                if (config.showSummary && review.summary != null) {
                    Text(
                        text = review.summary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // -------------------------
                // BODY
                // -------------------------
                val displayText = if (review.isSpoiler && !expanded) {
                    "Spoiler content hidden"
                } else {
                    review.body
                }

                Text(
                    text = displayText,
                    maxLines = if (expanded) Int.MAX_VALUE else config.maxCollapsedLines,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )

                // -------------------------
                // ACTION ROW
                // -------------------------
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        if (config.showUpvotes) {
                            Text(
                                text = "👍 ${review.upvotes}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        if (review.isSpoiler) {
                            Text(
                                text = "Spoiler",
                                color = Color.Red,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    if (config.enableExpand) {
                        Text(
                            text = if (expanded) "Less" else "More",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    expanded = !expanded
                                    onAction(ReviewAction.Expand(review))
                                }
                        )
                    }
                }
            }
        }
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

