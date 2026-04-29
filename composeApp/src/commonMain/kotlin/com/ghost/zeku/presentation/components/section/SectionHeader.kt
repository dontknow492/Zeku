package com.ghost.zeku.presentation.components.section

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
import com.ghost.zeku.presentation.components.media.list.ListCardShimmer
import com.ghost.zeku.presentation.components.media.list.MediaListCardConfig
import com.ghost.zeku.presentation.components.media.poster.PosterCardShimmer
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.view_all

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)?,
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

        if (onAction != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onAction() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = action ?: stringResource(Res.string.view_all),
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
fun SectionLoadingShimmer(layout: SectionLayout, count: Int) {

    when (layout) {

        is SectionLayout.HorizontalRow -> {
            Row(
                modifier = Modifier.padding(layout.contentPadding),
                horizontalArrangement = Arrangement.spacedBy(layout.itemSpacing)
            ) {
                repeat(count) {
                    PosterCardShimmer(config = PosterConfig())
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
                items(count) {
                    PosterCardShimmer(config = PosterConfig())
                }
            }
        }

        is SectionLayout.VerticalList -> {
            Column(
                modifier = Modifier.padding(layout.contentPadding),
                verticalArrangement = Arrangement.spacedBy(layout.itemSpacing)
            ) {
                repeat(count) {
                    ListCardShimmer(config = MediaListCardConfig())
                }
            }
        }
    }
}



