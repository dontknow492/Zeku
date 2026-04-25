package com.ghost.zeku.presentation.components.chapter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ghost.zeku.domain.model.media.Chapter

@Composable
fun ChapterCard(
    chapter: Chapter,
    config: ChapterCardConfig = ChapterCardConfig(),
    modifier: Modifier = Modifier,
    onClick: (Chapter) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scaleTarget = when {
        isPressed -> config.scaleOnPress
        isHovered -> config.scaleOnHover
        else -> 1f
    }

    val scale by animateFloatAsState(scaleTarget, config.animationSpec)

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        when (config.variant) {
            ChapterCardVariant.MODERN ->
                ModernChapterCard(chapter, config, interactionSource, onClick)

            ChapterCardVariant.MINIMAL ->
                MinimalChapterCard(chapter, config, interactionSource, onClick)

            ChapterCardVariant.COMPACT ->
                CompactChapterCard(chapter, config, interactionSource, onClick)
        }
    }
}


@Composable
private fun ModernChapterCard(
    chapter: Chapter,
    config: ChapterCardConfig,
    interactionSource: MutableInteractionSource,
    onClick: (Chapter) -> Unit
) {
    Card(
        onClick = { if (config.clickable) onClick(chapter) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(containerColor = config.containerColor)
    ) {
        Column(
            modifier = Modifier.padding(config.padding)
        ) {

            Text(
                text = "Chapter ${formatChapterNumber(chapter.number)}",
                color = config.accentColor,
                style = config.subtitleStyle
            )

            Text(
                text = chapter.title ?: "Untitled Chapter",
                color = config.titleColor,
                style = config.titleStyle,
                maxLines = 1
            )

            if (config.showVolume && chapter.volume != null) {
                Text(
                    text = "Volume ${chapter.volume}",
                    color = config.subtitleColor,
                    style = config.subtitleStyle
                )
            }
        }
    }
}


@Composable
private fun MinimalChapterCard(
    chapter: Chapter,
    config: ChapterCardConfig,
    interactionSource: MutableInteractionSource,
    onClick: (Chapter) -> Unit
) {
    Card(
        onClick = { if (config.clickable) onClick(chapter) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(containerColor = config.containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(config.height)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(config.padding)
        ) {

            // Chapter number block
            Text(
                text = formatChapterNumber(chapter.number),
                style = config.titleStyle,
                color = config.accentColor
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = chapter.title ?: "Chapter ${formatChapterNumber(chapter.number)}",
                    style = config.titleStyle,
                    color = config.titleColor,
                    maxLines = 1
                )

                if (config.showVolume && chapter.volume != null) {
                    Text(
                        text = "Vol ${chapter.volume}",
                        style = config.subtitleStyle,
                        color = config.subtitleColor
                    )
                }
            }
        }
    }
}


@Composable
private fun CompactChapterCard(
    chapter: Chapter,
    config: ChapterCardConfig,
    interactionSource: MutableInteractionSource,
    onClick: (Chapter) -> Unit
) {
    Card(
        onClick = { if (config.clickable) onClick(chapter) },
        interactionSource = interactionSource,
        shape = config.shape,
        colors = CardDefaults.cardColors(containerColor = config.containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = formatChapterNumber(chapter.number),
                style = config.subtitleStyle,
                color = config.accentColor
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = chapter.title ?: "Ch ${formatChapterNumber(chapter.number)}",
                style = config.titleStyle,
                color = config.titleColor,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 400)
@Composable
fun PreviewChapterVariants() {

    val sample = Chapter(
        id = "1",
        number = 10.5f,
        title = "A Turning Point",
        volume = 2
    )

    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Modern")
            ChapterCard(sample, ChapterCardConfig(variant = ChapterCardVariant.MODERN))

            Text("Minimal")
            ChapterCard(sample, ChapterCardConfig(variant = ChapterCardVariant.MINIMAL))

            Text("Compact")
            ChapterCard(sample, ChapterCardConfig(variant = ChapterCardVariant.COMPACT))
        }
    }
}