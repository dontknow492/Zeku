package com.ghost.zeku.presentation.screen.details


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.common.format
import com.ghost.zeku.domain.model.enum.*
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.presentation.common.HeroImage
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.common.chips.GenreChip
import com.ghost.zeku.presentation.common.chips.MyChips
import com.ghost.zeku.presentation.common.isWideScreen
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.character.MediaCharacterCard
import com.ghost.zeku.presentation.components.media.character.MediaCharacterCardConfig
import com.ghost.zeku.presentation.components.media.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.toPosterUiData
import com.ghost.zeku.presentation.components.media.relation.MediaRelationCard
import com.ghost.zeku.presentation.components.media.relation.MediaRelationCardConfig
import com.ghost.zeku.presentation.components.media.relation.RelationCardLayout
import com.ghost.zeku.presentation.components.media.review.ReviewCard
import com.ghost.zeku.presentation.components.section.MediaSection
import com.ghost.zeku.presentation.components.section.MediaSectionConfig
import com.ghost.zeku.presentation.components.section.PagedMediaSection
import com.ghost.zeku.presentation.navigation.Destination
import com.ghost.zeku.presentation.theme.AppTheme
import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailContract
import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailViewModel
import com.ghost.zeku.utils.toPagingItems
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import zeku.composeapp.generated.resources.*

@Composable
fun DetailScreen(
    mediaId: Int,
    mediaType: MediaType,
    viewModel: MediaDetailViewModel = koinViewModel(),
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isWideScreen = rememberPlatformConfiguration().isWideScreen
    val reviews = viewModel.reviews.collectAsLazyPagingItems()
    val recommendations = viewModel.recommendations.collectAsLazyPagingItems()
    val episodes = viewModel.episodes.collectAsLazyPagingItems()

    LaunchedEffect(mediaId, mediaType) {
        Napier.d(tag = "MediaDetailUI") { "🚀 Screen launched for ${mediaType.name} ID: $mediaId" }
        viewModel.onEvent(MediaDetailContract.Event.Load(mediaId, mediaType))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MediaDetailContract.Effect.Navigate -> {
                    Napier.d(tag = "MediaDetailUI") { "Navigating to ${effect.destination}" }
                    onNavigate(effect.destination)
                }

                is MediaDetailContract.Effect.OpenExternalLink -> {
                    Napier.d(tag = "MediaDetailUI") { "Opening external link: ${effect.link.url}" }
                    TODO("Not yet implemented")
                }

                is MediaDetailContract.Effect.PlayTrailer -> {
                    Napier.d(tag = "MediaDetailUI") { "Playing trailer: ${effect.trailerId}" }
                    TODO("Not yet implemented")
                }

                is MediaDetailContract.Effect.ShowMessage -> {
                    TODO("Implement snackbar messaging")
                }

                is MediaDetailContract.Effect.OpenChat -> {
                    Napier.d(tag = "MediaDetailUI") { "Opening chat room for media: ${effect.id}" }
                    TODO("Not yet implemented")
                }
            }
        }
    }

    MediaDetailContent(
        state = state,
        episodes = episodes,
        recommendations = recommendations,
        reviews = reviews,
        onEvent = viewModel::onEvent,
        config = MediaDetailUiConfig(),
        isWideScreen = isWideScreen,
    )
}

@Composable
fun MediaDetailContent(
    state: MediaDetailContract.State,
    episodes: LazyPagingItems<Episode>,
    recommendations: LazyPagingItems<Anime>,
    reviews: LazyPagingItems<Review>,
    onEvent: (MediaDetailContract.Event) -> Unit,
    config: MediaDetailUiConfig,
    isWideScreen: Boolean = false
) {
    val layoutDirection = LocalLayoutDirection.current
    val dimens = MediaDetailDimens

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            Modifier.padding(
                bottom = padding.calculateBottomPadding(),
                start = padding.calculateStartPadding(layoutDirection),
                end = padding.calculateEndPadding(layoutDirection)
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (isWideScreen) {
                    DesktopLayout(state, episodes, recommendations, reviews, config, onEvent)
                } else {
                    MobileLayout(state, episodes, recommendations, reviews, config, onEvent)
                }
            }

            // Floating chat button
            FloatingActionButton(
                onClick = {
                    Napier.d(tag = "MediaDetailUI") { "FAB Clicked: Open Chat" }
                    onEvent(MediaDetailContract.Event.OpenChat)
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimens.fabPadding)
                    .scale(1f)
                    .clip(RoundedCornerShape(dimens.cardCornerRadius))
            ) {
                Icon(Icons.Outlined.ChatBubble, contentDescription = stringResource(Res.string.chat))
            }
        }
    }
}

// ---------- Desktop Layout ----------
@Composable
private fun DesktopLayout(
    state: MediaDetailContract.State,
    episodes: LazyPagingItems<Episode>,
    recommendations: LazyPagingItems<Anime>,
    reviews: LazyPagingItems<Review>,
    config: MediaDetailUiConfig,
    onEvent: (MediaDetailContract.Event) -> Unit
) {
    val dimens = MediaDetailDimens
    val mediaId = state.id

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        // 1. HERO SECTION
        item {
            Box(
                modifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val overlap = config.desktopHeroMetaOffset.roundToPx()
                    layout(placeable.width, placeable.height - overlap) {
                        placeable.placeRelative(0, 0)
                    }
                }
            ) {
                HeroSection(state, isDesktop = true, config, onEvent = onEvent)
            }
        }

        // 2. SPLIT ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(config.desktopItemSpacing)
            ) {
                // LEFT PANEL
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(config.desktopItemSpacing)
                ) {
                    SynopsisSection(state.description)
                    TagsSection(state.tags, onEvent)

                    AnimatedVisibility(config.showTrailer) {
                        TrailerSection(state.trailer, onEvent)
                    }

                    CharacterSection(
                        modifier = Modifier,
                        characters = state.characters,
                        sectionConfig = config.characterSection,
                        cardConfig = config.characterCard,
                        onViewAllClick = { onEvent(MediaDetailContract.Event.ViewAllCharacters(mediaId = mediaId)) },
                        onCharacterClick = { onEvent(MediaDetailContract.Event.ViewCharacter(it)) }
                    )

                    val reviewsPreview = reviews.itemSnapshotList.take(6)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
                    ) {
                        SectionHeader(
                            title = stringResource(Res.string.reviews),
                            action = stringResource(Res.string.view_all),
                            onAction = { onEvent(MediaDetailContract.Event.ViewAllReviews(mediaId = mediaId)) }
                        )
                        reviewsPreview.forEach { review ->
                            if (review != null) {
                                ReviewCard(
                                    modifier = Modifier.padding(start = dimens.horizontalPadding),
                                    review = review,
                                    config = config.reviewCard,
                                    onAction = { onEvent(MediaDetailContract.Event.OnReviewAction(it)) }
                                )
                            }
                        }
                    }
                }

                // RIGHT PANEL (Sidebar)
                Column(
                    modifier = Modifier
                        .width(config.desktopSideBarWidth)
                        .padding(config.desktopItemSpacing),
                    verticalArrangement = Arrangement.spacedBy(config.desktopItemSpacing)
                ) {
                    InfoPanel(Modifier, state, onEvent = onEvent)

                    AnimatedVisibility(config.showExternalLinks) {
                        ExternalLinksPanel(
                            state.externalLinks,
                            isMobile = false,
                            onClick = { onEvent(MediaDetailContract.Event.OpenExternalLink(it)) }
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                        modifier = Modifier.padding(start = dimens.horizontalPadding)
                    ) {
                        SectionHeader(
                            title = stringResource(Res.string.relations),
                            action = stringResource(Res.string.view_all),
                            onAction = { onEvent(MediaDetailContract.Event.ViewAllRelations(mediaId = mediaId)) },
                        )
                        state.relations.forEach {
                            MediaRelationCard(
                                relation = it,
                                config = config.relationCard.copy(layout = RelationCardLayout.WIDE),
                                onClick = { onEvent(MediaDetailContract.Event.ViewRelation(it)) }
                            )
                        }
                    }

                    val previewItems = recommendations.itemSnapshotList.items.take(6)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                        modifier = Modifier.padding(start = dimens.horizontalPadding)
                    ) {
                        SectionHeader(
                            title = stringResource(Res.string.more_like_this),
                            action = stringResource(Res.string.view_all),
                            onAction = { onEvent(MediaDetailContract.Event.ViewAllRecommendations(mediaId = mediaId)) },
                        )
                        previewItems.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium)) {
                                row.forEach { item ->
                                    MediaPosterCard(
                                        data = item.toPosterUiData(),
                                        modifier = Modifier.weight(1f),
                                        onAction = { onEvent(MediaDetailContract.Event.OnMediaAction(it)) },
                                        config = config.recommendationCard
                                    )
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Mobile Layout ----------
@Composable
private fun MobileLayout(
    state: MediaDetailContract.State,
    episodes: LazyPagingItems<Episode>,
    recommendations: LazyPagingItems<Anime>,
    reviews: LazyPagingItems<Review>,
    config: MediaDetailUiConfig,
    onEvent: (MediaDetailContract.Event) -> Unit
) {
    val dimens = MediaDetailDimens
    val mediaId = state.id

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(config.androidItemSpacing)
    ) {
        // 1. HERO OVERLAP
        item {
            Box(
                modifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val overlap = config.androidHeroMetaOffset.roundToPx()
                    layout(placeable.width, placeable.height - overlap) {
                        placeable.placeRelative(0, 0)
                    }
                }
            ) {
                HeroSection(state, isDesktop = false, config, onEvent = onEvent)
            }
        }

        // Watch button right on top of the hero
        item {
            Button(
                onClick = {
                    Napier.d(tag = "MediaDetailUI") { "Mobile Watch Now clicked" }
                    onEvent(MediaDetailContract.Event.StartWatching)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding)
                    .height(dimens.buttonHeight),
                shape = RoundedCornerShape(dimens.cardCornerRadius)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(Modifier.width(dimens.spacingSmall))
                Text(stringResource(Res.string.watch_now), style = MaterialTheme.typography.titleMedium)
            }
        }

        item { SynopsisSection(state.description) }

        item { TagsSection(state.tags, onEvent) }

        item { InfoPanel(Modifier.padding(horizontal = dimens.horizontalPadding), state, onEvent = onEvent) }

        if (config.showTrailer) {
            item { TrailerSection(state.trailer, onEvent) }
        }

        item {
            CharacterSection(
                modifier = Modifier,
                characters = state.characters,
                sectionConfig = config.characterSection,
                cardConfig = config.characterCard,
                onViewAllClick = { onEvent(MediaDetailContract.Event.ViewAllCharacters(mediaId = mediaId)) },
                onCharacterClick = { onEvent(MediaDetailContract.Event.ViewCharacter(it)) }
            )
        }

        if (config.showExternalLinks) {
            item {
                ExternalLinksPanel(
                    state.externalLinks,
                    isMobile = true,
                    onClick = { onEvent(MediaDetailContract.Event.OpenExternalLink(it)) }
                )
            }
        }

        item {
            RelationSection(
                modifier = Modifier,
                relations = state.relations,
                sectionConfig = config.relationSection,
                cardConfig = config.relationCard,
                onViewAllClick = { onEvent(MediaDetailContract.Event.ViewAllRelations(mediaId = mediaId)) },
                onRelationClick = { onEvent(MediaDetailContract.Event.ViewRelation(it)) }
            )
        }

        item {
            RecommendationSection(
                modifier = Modifier,
                recommendations = recommendations,
                sectionConfig = config.recommendationSection,
                cardConfig = config.recommendationCard,
                onAction = { onEvent(MediaDetailContract.Event.OnMediaAction(it)) },
                onViewAllClick = { onEvent(MediaDetailContract.Event.ViewAllRecommendations(mediaId = mediaId)) }
            )
        }

        item {
            SectionHeader(
                modifier = Modifier.padding(horizontal = dimens.horizontalPadding),
                title = stringResource(Res.string.reviews),
                action = stringResource(Res.string.view_all),
                onAction = { onEvent(MediaDetailContract.Event.ViewAllReviews(mediaId = mediaId)) }
            )
        }

        items(reviews.itemCount) { index ->
            val review = reviews[index]
            if (review != null) {
                ReviewCard(
                    review = review,
                    config = config.reviewCard,
                    modifier = Modifier.animateItem().padding(horizontal = dimens.horizontalPadding),
                    onAction = { onEvent(MediaDetailContract.Event.OnReviewAction(it)) }
                )
            }
        }
    }
}

// ---------- Hero Section ----------
@Composable
private fun HeroSection(
    state: MediaDetailContract.State,
    isDesktop: Boolean,
    config: MediaDetailUiConfig,
    onEvent: (MediaDetailContract.Event) -> Unit
) {
    val dimens = MediaDetailDimens

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isDesktop) config.desktopHeroBannerSize else config.androidHeroBannerHeight)
    ) {
        val imageUrl = state.bannerImage ?: state.coverImage

        HeroImage(
            modifier = Modifier.matchParentSize(),
            imageUrl = imageUrl,
            isDesktop = isDesktop,
            blurRadius = dimens.blurRadius,
        )

        // 4. Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(dimens.horizontalPadding)
                .padding(bottom = if (isDesktop) config.desktopHeroMetaOffset else config.androidHeroMetaOffset)
        ) {
            if (isDesktop) {
                Row(verticalAlignment = Alignment.Bottom) {
                    MediaAsyncImage(
                        url = state.coverImage,
                        contentDescription = stringResource(Res.string.poster),
                        modifier = Modifier
                            .width(dimens.posterWidth)
                            .aspectRatio(dimens.posterAspectRatio)
                            .clip(RoundedCornerShape(dimens.cardCornerRadius))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(dimens.cardCornerRadius))
                    )
                    Spacer(Modifier.width(dimens.spacingLarge))
                    Column(modifier = Modifier.weight(1f)) {
                        MetadataAndActions(state, isDesktop = true, onEvent = onEvent)
                    }
                }
            } else {
                MetadataAndActions(state, isDesktop = false, onEvent = onEvent)
            }
        }
    }
}

// ---------- Metadata & Actions ----------
@Composable
private fun MetadataAndActions(
    state: MediaDetailContract.State,
    isDesktop: Boolean,
    onEvent: (MediaDetailContract.Event) -> Unit
) {
    val dimens = MediaDetailDimens

    // Genres
    Row(horizontalArrangement = Arrangement.spacedBy(dimens.chipSpacing)) {
        state.genres.forEach { genre ->
            GenreChip(onClick = {
                Napier.d(tag = "MediaDetailUI") { "Genre Clicked: $genre" }
                onEvent(MediaDetailContract.Event.OnGenreClick(genre))
            }, genre)
        }
    }
    Spacer(Modifier.height(dimens.spacingSmall))

    Text(
        text = state.title,
        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(Modifier.height(dimens.spacingSmall))

    // Determine the main display creator (Animation Studio > Any Studio > Any Staff)
    val displayCreator = state.studios.firstOrNull { it.isAnimationStudio }?.name
        ?: state.studios.firstOrNull()?.name
        ?: state.staff.firstOrNull()?.name
        ?: stringResource(Res.string.unknown)

    // Rating, Release date, Creator
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(dimens.iconSize)
        )
        Text(
            text = " ${state.averageScore?.let { "$it" } ?: stringResource(Res.string.unknown)}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(" ${stringResource(Res.string.dot_separator)} ", color = MaterialTheme.colorScheme.outline)
        Text(
            text = state.startDate?.format() ?: stringResource(Res.string.unknown),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        )
        Text(" ${stringResource(Res.string.dot_separator)} ", color = MaterialTheme.colorScheme.outline)
        Text(
            text = displayCreator,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        )
    }

    if (isDesktop) {
        Spacer(Modifier.height(dimens.spacingMedium))
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium)) {
            Button(
                onClick = {
                    Napier.d(tag = "MediaDetailUI") { "Desktop Watch Now clicked" }
                    onEvent(MediaDetailContract.Event.StartWatching)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                Text(stringResource(Res.string.watch_now), color = MaterialTheme.colorScheme.onSecondary)
            }
            OutlinedButton(
                onClick = {
                    Napier.d(tag = "MediaDetailUI") { "Toggle Watchlist clicked" }
                    onEvent(MediaDetailContract.Event.ToggleWatchlist)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text(stringResource(Res.string.watchlist))
            }
        }
    }
}

@Composable
private fun SynopsisSection(synopsis: String?) {
    val dimens = MediaDetailDimens
    val text = synopsis?.trim().takeUnless { it.isNullOrBlank() }
        ?: stringResource(Res.string.no_description)

    var expanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader(
                title = stringResource(Res.string.synopsis),
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        onClick = {
                            clipboard.setText(AnnotatedString(text))
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (expanded) "Collapse" else "Expand") },
                        onClick = {
                            expanded = !expanded
                            showMenu = false
                        }
                    )
                }
            }
        }

        Box {
            SelectionContainer {
                Text(
                    text = text,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!expanded && text.length > 150) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                startY = 100f
                            )
                        )
                )
            }
        }

        if (text.length > 150) {
            Text(
                text = if (expanded) "Show less" else "Read more",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }
    }
}

@Composable
private fun TagsSection(
    tags: List<MediaTag>,
    onEvent: (MediaDetailContract.Event) -> Unit
) {
    if (tags.isEmpty()) return
    val dimens = MediaDetailDimens

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
    ) {
        SectionHeader(title = "Tags")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(dimens.chipSpacing),
            verticalArrangement = Arrangement.spacedBy(dimens.chipSpacing)
        ) {
            tags.forEach { tag ->
                MyChips(
                    onClick = {
                        Napier.d(tag = "MediaDetailUI") { "Tag Clicked: ${tag.name}" }
                        onEvent(MediaDetailContract.Event.OnTagClick(tag))
                    },
                    text = {
                        Text(
                            text = "${tag.name}${if (tag.rank != null) " (${tag.rank}%)" else ""}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TrailerSection(
    trailer: MediaTrailer?,
    onEvent: (MediaDetailContract.Event) -> Unit
) {
    val dimens = MediaDetailDimens
    val hasTrailer = trailer?.id?.isNotBlank() == true

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = tween(200)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(title = stringResource(Res.string.official_trailer))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(dimens.trailerAspectRatio)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(dimens.cardCornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .clickable(
                    enabled = hasTrailer,
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    trailer?.id?.let {
                        Napier.d(tag = "MediaDetailUI") { "Trailer Video Clicked" }
                        onEvent(MediaDetailContract.Event.PlayTrailer(it))
                    }
                }
        ) {
            MediaAsyncImage(
                url = trailer?.thumbnail ?: "",
                contentDescription = stringResource(Res.string.official_trailer),
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(dimens.trailerPlayIconSize * 1.6f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = stringResource(Res.string.play),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(dimens.trailerPlayIconSize)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimens.spacingMedium)
            ) {
                Text(
                    text = trailer?.title ?: stringResource(Res.string.official_trailer),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2
                )
                if (!hasTrailer) {
                    Text(
                        text = stringResource(Res.string.trailer_not_available),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (action != null) {
            TextButton(onClick = onAction ?: {}) {
                Text(action, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ---------- Sections ----------
@Composable
fun CharacterSection(
    modifier: Modifier = Modifier,
    characters: List<MediaCharacter>,
    sectionConfig: MediaSectionConfig,
    cardConfig: MediaCharacterCardConfig,
    onCharacterClick: (MediaCharacter) -> Unit,
    onViewAllClick: () -> Unit,
) {
    MediaSection(
        title = stringResource(Res.string.characters),
        items = characters,
        onViewAllClick = onViewAllClick,
        config = sectionConfig,
        modifier = modifier,
        key = null,
        isLoading = false,
    ) { character, mod ->
        MediaCharacterCard(character = character, config = cardConfig, modifier = mod, onClick = onCharacterClick)
    }
}

@Composable
fun RelationSection(
    modifier: Modifier = Modifier,
    relations: List<MediaRelation>,
    sectionConfig: MediaSectionConfig,
    cardConfig: MediaRelationCardConfig,
    onRelationClick: (MediaRelation) -> Unit,
    onViewAllClick: () -> Unit
) {
    MediaSection(
        title = stringResource(Res.string.relations),
        items = relations.take(1),
        onViewAllClick = onViewAllClick,
        config = sectionConfig,
        key = null,
        isLoading = false,
        modifier = modifier
    ) { relation, mod ->
        MediaRelationCard(relation = relation, config = cardConfig, modifier = mod, onClick = onRelationClick)
    }
}

@Composable
fun RecommendationSection(
    modifier: Modifier = Modifier,
    recommendations: LazyPagingItems<Anime>,
    sectionConfig: MediaSectionConfig,
    cardConfig: PosterConfig,
    onViewAllClick: () -> Unit,
    onAction: (MediaAction) -> Unit
) {
    PagedMediaSection(
        modifier = modifier,
        title = stringResource(Res.string.more_like_this),
        config = sectionConfig,
        items = recommendations,
        onViewAllClick = onViewAllClick
    ) { media, mod ->
        MediaPosterCard(data = media.toPosterUiData(), modifier = mod, onAction = onAction, config = cardConfig)
    }
}

@Composable
private fun ExternalLinksPanel(
    links: List<ExternalLink>,
    isMobile: Boolean = false,
    onClick: (ExternalLink) -> Unit
) {
    val dimens = MediaDetailDimens
    val safeLinks = remember(links) { links.filter { it.url.isNotBlank() }.distinctBy { it.url } }
    if (safeLinks.isEmpty()) return

    val title = stringResource(Res.string.watch_follow)

    if (isMobile) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.horizontalPadding)) {
            SectionHeader(title = title)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium)) {
                items(items = safeLinks, key = { it.url }) { link ->
                    ExternalLinkItem(link = link, onClick = onClick)
                }
            }
        }
    } else {
        GlassCard(shape = RoundedCornerShape(dimens.glassCardCornerRadius)) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = dimens.spacingMedium, vertical = dimens.verticalPadding)
            ) {
                SectionHeader(title = title)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
                ) {
                    safeLinks.forEach { link -> ExternalLinkItem(link = link, onClick = onClick) }
                }
            }
        }
    }
}

@Composable
private fun ExternalLinkItem(link: ExternalLink, onClick: (ExternalLink) -> Unit) {
    val fallbackIcon = remember(link.site) {
        when {
            link.site.contains("twitter", true) -> Icons.Default.Share
            link.site.contains("youtube", true) -> Icons.Default.PlayArrow
            link.site.contains("crunchyroll", true) -> Icons.Default.PlayArrow
            else -> Icons.Default.Link
        }
    }

    MyChips(
        onClick = { if (link.url.isNotBlank()) onClick(link) },
        shape = RoundedCornerShape(percent = 25),
        leadingIcon = {
            if (link.iconUrl.isNullOrBlank()) {
                Icon(imageVector = fallbackIcon, contentDescription = link.site, modifier = Modifier.size(18.dp))
                return@MyChips
            }
            AsyncImage(
                model = link.iconUrl,
                contentDescription = link.site,
                modifier = Modifier.size(18.dp),
                placeholder = rememberVectorPainter(Icons.Default.Web),
                error = rememberVectorPainter(Icons.Default.BrokenImage),
                fallback = rememberVectorPainter(Icons.Default.Link),
                contentScale = ContentScale.Fit
            )
        },
        text = {
            Text(
                text = link.site.ifBlank { stringResource(Res.string.open_link) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

// ---------------------- Info Panel ----------------------

@Composable
private fun InfoPanel(
    modifier: Modifier = Modifier,
    state: MediaDetailContract.State,
    onEvent: (MediaDetailContract.Event) -> Unit
) {
    val dimens = MediaDetailDimens
    val colors = MaterialTheme.colorScheme

    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.glassCardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
        ) {
            Text(
                text = stringResource(Res.string.information),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )

            InfoGroup {
                InfoRow(stringResource(Res.string.status), state.status.name.replace("_", " "))
                InfoRow(stringResource(Res.string.format), state.format.name.replace("_", " "))
                InfoRowIfValid("Source", state.sourceMaterial?.name?.replace("_", " "))
                InfoRowIfValid(stringResource(Res.string.premiered), state.startDate?.format())
                InfoRowIfValid("Season", state.season?.name?.plus(state.seasonYear?.let { " $it" } ?: ""))
                InfoRowIfValid("Broadcast", state.broadcastString)
            }

            InfoGroup {
                when (state.type) {
                    MediaType.ANIME -> {
                        InfoRowIfValid(stringResource(Res.string.episodes), state.totalEpisodes)
                        InfoRowIfValid(stringResource(Res.string.duration), state.durationPerEpisode?.let { "$it min" })
                    }

                    MediaType.MANGA -> {
                        InfoRowIfValid(stringResource(Res.string.volumes), state.totalVolumes)
                        InfoRowIfValid(stringResource(Res.string.chapters), state.totalChapters)
                    }

                    else -> {}
                }
            }

            InfoGroup {
                InfoRowIfValid(stringResource(Res.string.score), state.averageScore?.let { "$it%" })
                InfoRowIfValid("Mean Score", state.meanScore)
                InfoRowIfValid("Rank", state.rank?.let { "#$it" })
                InfoRowIfValid("Popularity", state.popularity?.let { "#$it" })
                InfoRowIfValid("Favorites", state.favourites)
            }

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))

            // Map all structured staff & studios into our uniform CreditType
            val credits = remember(state.staff, state.studios) {
                state.staff.map { CreditType.Staff(it) } + state.studios.map { CreditType.Studio(it) }
            }

            if (credits.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)) {
                    Text(
                        text = stringResource(Res.string.producers),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(dimens.chipSpacing),
                        verticalArrangement = Arrangement.spacedBy(dimens.chipSpacing)
                    ) {
                        credits.forEach { credit ->
                            CreditChip(
                                credit = credit,
                                onClick = {
                                    Napier.d(tag = "MediaDetailUI") { "Credit Clicked: ${credit.name} (${credit.label})" }
                                    onEvent(credit.toEvent())
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// Sealed class for mapping Domain Staff/Studio cleanly to UI Chips
sealed class CreditType(val name: String, val label: String, val icon: ImageVector) {
    data class Staff(val staff: MediaStaff) : CreditType(staff.name, staff.role, Icons.Filled.Person)
    data class Studio(val studio: MediaStudio) :
        CreditType(studio.name, if (studio.isAnimationStudio) "Animation Studio" else "Producer", Icons.Filled.Business)

    fun toEvent(): MediaDetailContract.Event = when (this) {
        is Staff -> MediaDetailContract.Event.OnStaffClick(staff)
        is Studio -> MediaDetailContract.Event.OnStudioClick(studio)
    }
}

@Composable
private fun InfoGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun CreditChip(credit: CreditType, onClick: () -> Unit) {
    MyChips(
        onClick = onClick,
        text = {
            Column {
                Text(
                    text = credit.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = credit.name, style = MaterialTheme.typography.bodyMedium)
            }
        },
        leadingIcon = { Icon(credit.icon, contentDescription = credit.label) }
    )
}

@Composable
private fun InfoRowIfValid(label: String, value: Any?) {
    val text = when (value) {
        null -> null
        is String -> value.takeIf { it.isNotBlank() }
        else -> value.toString()
    }
    if (text != null) InfoRow(label, text)
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) { content() }
}

// ---------- Previews ----------
@Preview(showBackground = true, widthDp = 450)
@Composable
fun PreviewMediaDetailContent() {

    val previewAnimeState = MediaDetailContract.State(
        id = 40748,
        type = MediaType.ANIME,
        source = ProviderType.ANILIST,

        // Identity
        title = "Jujutsu Kaisen",
        nativeTitle = "呪術廻戦",
        synonyms = listOf("JJK", "Sorcery Fight"),
        countryOfOrigin = "JP",

        // Visuals & Text
        coverImage = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx113415-bbBWj4pEFseh.jpg",
        bannerImage = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/113415-jQBSkxWAAk83.jpg",
        extraPictures = listOf(
            "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx113415-bbBWj4pEFseh.jpg"
        ),
        description = "Idly indulging in baseless paranormal activities with the Occult Club, high schooler Yuuji Itadori spends his days at either the clubroom or the hospital, where he visits his bedridden grandfather. However, this leisurely lifestyle soon takes a turn for the bizarre when he unknowingly encounters a cursed item. Triggering a chain of supernatural occurrences, Yuuji finds himself suddenly thrust into the world of Curses—terrible beings formed from human malice and negativity—after swallowing the said item, revealed to be a finger belonging to the demon Sukuna Ryoumen, the \"King of Curses.\"\n\nYuuji experiences first-hand the threat these Curses pose to society as he discovers his own newly found magic powers. Introduced to the Tokyo Metropolitan Curse Technical School, he begins to walk down a path from which he cannot return—the path of a Jujutsu sorcerer.",
        background = "Jujutsu Kaisen won the Anime of the Year award at the 2021 Crunchyroll Anime Awards.",

        // Metadata
        genres = listOf("Action", "Dark Fantasy", "Supernatural", "Shounen"),
        tags = listOf(
            MediaTag("Male Protagonist", rank = 96),
            MediaTag("Curses", rank = 92),
            MediaTag("School", rank = 85),
            MediaTag("Gore", rank = 78),
            MediaTag("Hand-to-Hand Combat", rank = 88)
        ),
        status = MediaReleaseStatus.FINISHED,
        format = MediaFormat.TV,
        sourceMaterial = MediaSourceMaterial.MANGA,
        isAdult = false,

        // Dates & Schedules
        startDate = MediaDate(2020, 10, 3),
        endDate = MediaDate(2021, 3, 27),
        season = MediaSeason.FALL,
        seasonYear = 2020,
        broadcastString = "Saturdays at 01:25 (JST)",

        // Statistics
        averageScore = 86.0,
        meanScore = 86.4,
        popularity = 1354000,
        favourites = 89000,
        rank = 34,

        // Anime Specific
        totalEpisodes = 24,
        durationPerEpisode = 24,
        contentRating = "R - 17+ (violence & profanity)",
        nextAiringEpisode = null, // Finished airing
        studios = listOf(
            MediaStudio(1, "MAPPA", isAnimationStudio = true),
            MediaStudio(2, "TOHO animation", isAnimationStudio = false),
            MediaStudio(3, "Shueisha", isAnimationStudio = false)
        ),

        // Relational Data (Eager)
        trailer = MediaTrailer(
            id = "pkKu9hLT-t8",
            site = "youtube",
            thumbnail = "https://i.ytimg.com/vi/pkKu9hLT-t8/hqdefault.jpg",
            title = "Official Trailer #1"
        ),
        externalLinks = listOf(
            ExternalLink(
                "https://www.crunchyroll.com/jujutsu-kaisen",
                "Crunchyroll",
                "https://crunchyroll.com/favicon.ico"
            ),
            ExternalLink("https://twitter.com/animejujutsu", "Twitter", null),
            ExternalLink("https://jujutsukaisen.jp/", "Official Site", null)
        ),
        characters = listOf(
            MediaCharacter(
                1,
                "Yuuji Itadori",
                "https://s4.anilist.co/file/anilistcdn/character/large/b127212-FVm2tD0erQ5B.png",
                CharacterRole.MAIN
            ),
            MediaCharacter(
                2,
                "Satoru Gojou",
                "https://s4.anilist.co/file/anilistcdn/character/large/b127691-9zqh1xpIubn7.png",
                CharacterRole.MAIN
            ),
            MediaCharacter(
                3,
                "Megumi Fushiguro",
                "https://s4.anilist.co/file/anilistcdn/character/large/b126635-L0y3I92JSUkN.png",
                CharacterRole.MAIN
            ),
            MediaCharacter(
                4,
                "Nobara Kugisaki",
                "https://s4.anilist.co/file/anilistcdn/character/large/b126636-1eAIE0v9KioO.png",
                CharacterRole.MAIN
            ),
            MediaCharacter(
                5,
                "Sukuna Ryoumen",
                "https://s4.anilist.co/file/anilistcdn/character/large/b127213-9U3P91B8rZk0.png",
                CharacterRole.SUPPORTING
            )
        ),
        relations = listOf(
            MediaRelation(
                131573,
                RelationType.SEQUEL,
                MediaTitle(english = "JUJUTSU KAISEN 0"),
                "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx131573-0IAlR0R4Y4rO.jpg",
                MediaType.ANIME,
                MediaFormat.MOVIE
            ),
            MediaRelation(
                145064,
                RelationType.SEQUEL,
                MediaTitle(english = "JUJUTSU KAISEN Season 2"),
                "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx145064-xP8UeP3AIO06.jpg",
                MediaType.ANIME,
                MediaFormat.TV
            )
        ),
        staff = listOf(
            MediaStaff(
                1,
                "Gege Akutami",
                "Original Creator",
                "https://s4.anilist.co/file/anilistcdn/staff/large/n131652-5g8wR83h3dEI.png"
            ),
            MediaStaff(2, "Sunghoo Park", "Director", null),
            MediaStaff(3, "Hiroshi Seko", "Series Composition", null)
        ),

        // User Context (Simulating a user who is currently watching it)
        trackEntry = TrackEntry(
            entryId = 999,
            mediaId = 40748,
            status = TrackStatus.CURRENT,
            progress = 12, // Watched 12 out of 24
            totalProgress = 24,
            score = 9.0
        ),

        isLoading = false,
        error = null
    )

    val previewMangaState = MediaDetailContract.State(
        id = 30002,
        type = MediaType.MANGA,
        source = ProviderType.ANILIST,

        title = "Berserk",
        nativeTitle = "ベルセルク",
        synonyms = listOf("Berserk: The Prototype"),
        countryOfOrigin = "JP",

        coverImage = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/large/bx30002-7KuhiROEEAie.jpg",
        bannerImage = "https://s4.anilist.co/file/anilistcdn/media/manga/banner/30002-Hj8F6z1H9Q10.jpg",
        description = "Guts, a former mercenary now known as the \"Black Swordsman,\" is out for revenge. After a tumultuous childhood, he finally finds someone he respects and believes he can trust, only to have everything fall apart when this person takes away everything important to Guts for the purpose of fulfilling his own desires. Now marked for death, Guts becomes condemned to a fate in which he is relentlessly pursued by demonic beings.\n\nSetting out on a dreadful quest riddled with misfortune, Guts, armed with a massive sword and an iron will, will let nothing stop him, not even death itself, until he is finally able to take the head of the one who stripped him—and his loved one—of their humanity.",
        background = "Berserk won the Award for Excellence at the 6th Tezuka Osamu Cultural Prize in 2002.",

        genres = listOf("Action", "Adventure", "Drama", "Fantasy", "Horror", "Psychological"),
        tags = listOf(
            MediaTag("Dark Fantasy", rank = 98),
            MediaTag("Tragedy", rank = 95),
            MediaTag("Demons", rank = 92),
            MediaTag("Gore", rank = 90),
            MediaTag("Revenge", rank = 89)
        ),
        status = MediaReleaseStatus.RELEASING,
        format = MediaFormat.MANGA,
        sourceMaterial = MediaSourceMaterial.ORIGINAL,
        isAdult = true, // NSFW flag

        startDate = MediaDate(1989, 8, 25),
        endDate = null, // Still publishing

        // Empty for Manga
        season = null,
        seasonYear = null,
        broadcastString = null,
        totalEpisodes = null,
        durationPerEpisode = null,
        nextAiringEpisode = null,
        studios = emptyList(),
        trailer = null,

        averageScore = 93.0,
        meanScore = 93.8,
        popularity = 645000,
        favourites = 125000,
        rank = 1, // #1 Ranked Manga

        // Manga Specific
        totalChapters = null, // Ongoing
        totalVolumes = null,  // Ongoing
        serializations = listOf("Young Animal"),

        externalLinks = listOf(
            ExternalLink("https://www.younganimal.com/title/berserk/", "Young Animal", null)
        ),
        characters = listOf(
            MediaCharacter(
                1,
                "Guts",
                "https://s4.anilist.co/file/anilistcdn/character/large/b422-tH4QeH21b6B4.png",
                CharacterRole.MAIN
            ),
            MediaCharacter(
                2,
                "Griffith",
                "https://s4.anilist.co/file/anilistcdn/character/large/b423-kO12BszQ0aBc.png",
                CharacterRole.MAIN
            ),
            MediaCharacter(
                3,
                "Casca",
                "https://s4.anilist.co/file/anilistcdn/character/large/b424-Q3wX4t5Nl8rJ.png",
                CharacterRole.MAIN
            )
        ),
        relations = listOf(
            MediaRelation(
                33,
                RelationType.ADAPTATION,
                MediaTitle(english = "Berserk (1997)"),
                "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx33-10L61ZBEhGqg.jpg",
                MediaType.ANIME,
                MediaFormat.TV
            ),
            MediaRelation(
                10218,
                RelationType.ADAPTATION,
                MediaTitle(english = "Berserk: The Golden Age Arc I"),
                "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx10218-WjPndIu0D5s1.png",
                MediaType.ANIME,
                MediaFormat.MOVIE
            )
        ),
        staff = listOf(
            MediaStaff(
                1,
                "Kentarou Miura",
                "Story & Art",
                "https://s4.anilist.co/file/anilistcdn/staff/large/n96898-rZk0R1Z2Z9U1.png"
            ),
            MediaStaff(2, "Studio Gaga", "Art", null)
        ),

        // User Context (Simulating a user planning to read it)
        trackEntry = TrackEntry(
            entryId = 1002,
            mediaId = 30002,
            status = TrackStatus.PLANNING,
            progress = 0,
            totalProgress = null,
            score = null
        ),

        isLoading = false,
        error = null
    )

    val state = MediaDetailContract.State(
        id = 1,
        type = MediaType.ANIME,
        source = ProviderType.MYANIMELIST,
        title = "Jujutsu Kaisen",
        description = "A boy swallows a cursed finger...",
        coverImage = "https://www.themoviedb.org/t/p/w1280/fHpKWq9ayzSk8nSwqRuaAUemRKh.jpg",
        bannerImage = "https://media.themoviedb.org/t/p/w1066_and_h600_face/gmECX1DvFgdUPjtio2zaL8BPYPu.jpg",
        genres = listOf("Action", "Supernatural", "Shounen"),
        tags = listOf(MediaTag("Male Protagonist", rank = 90), MediaTag("Curses", rank = 85)),
        averageScore = 87.0,
        popularity = 124500,
        rank = 24,
        sourceMaterial = MediaSourceMaterial.MANGA,
        startDate = MediaDate(2020, 10, 3),
        season = MediaSeason.FALL,
        seasonYear = 2020,
        studios = listOf(MediaStudio(1, "MAPPA", true), MediaStudio(2, "TOHO animation", false)),
        staff = listOf(MediaStaff(1, "Gege Akutami", "Original Creator")),
        characters = listOf(
            MediaCharacter(
                1,
                "Yuji Itadori",
                "https://s4.anilist.co/file/anilistcdn/character/large/b127212-FVm2tD0erQ5B.png",
                CharacterRole.MAIN
            )
        ),
        isLoading = false,
        error = null
    )

    val isWideScreen = rememberPlatformConfiguration().isWideScreen

    AppTheme {
        MediaDetailContent(
            state = previewAnimeState,
            episodes = emptyList<Episode>().toPagingItems(),
            recommendations = emptyList<Anime>().toPagingItems(),
            reviews = emptyList<Review>().toPagingItems(),
            onEvent = {},
            config = MediaDetailUiConfig(),
            isWideScreen = isWideScreen
        )
    }
}