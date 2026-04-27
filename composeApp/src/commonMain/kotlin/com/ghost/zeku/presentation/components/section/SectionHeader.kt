package com.ghost.zeku.presentation.components.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(
    title: String,
    onViewAllClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom // Aligns the button nicely with the baseline of the title
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (onViewAllClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onViewAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View All $title",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


/**
 * A beautiful, generic loading shimmer that adapts to the section layout.
 */
@Composable
fun SectionLoadingShimmer(config: MediaSectionConfig) {

    when (val layout = config.layout) {

        is SectionLayout.HorizontalRow -> {
            Row(
                modifier = Modifier.padding(layout.contentPadding),
                horizontalArrangement = Arrangement.spacedBy(layout.itemSpacing)
            ) {
                repeat(config.shimmerItemCount) {
                    ShimmerPoster()
                }
            }
        }

        is SectionLayout.Grid -> {
            LazyVerticalGrid(
                columns = when (layout.columns) {
                    is GridType.Fixed -> GridCells.Fixed(layout.columns.count)
                    is GridType.Adaptive -> GridCells.Adaptive(layout.columns.minSize)
                },
                contentPadding = layout.contentPadding,
                horizontalArrangement = Arrangement.spacedBy(layout.horizontalSpacing),
                verticalArrangement = Arrangement.spacedBy(layout.verticalSpacing)
            ) {
                items(config.shimmerItemCount) {
                    ShimmerPoster()
                }
            }
        }

        is SectionLayout.VerticalList -> {
            Column(
                modifier = Modifier.padding(layout.contentPadding),
                verticalArrangement = Arrangement.spacedBy(layout.itemSpacing)
            ) {
                repeat(config.shimmerItemCount) {
                    ShimmerListItem()
                }
            }
        }
    }
}


@Composable
fun ShimmerPoster() {
    Box(
        modifier = Modifier
            .width(140.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    )
}

@Composable
fun ShimmerListItem() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
            Box(
                Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        }
    }
}