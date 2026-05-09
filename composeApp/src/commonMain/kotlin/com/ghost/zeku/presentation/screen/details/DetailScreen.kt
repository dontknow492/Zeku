package com.ghost.zeku.presentation.screen.details


import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.ghost.zeku.domain.model.media.format
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.presentation.common.*
import com.ghost.zeku.presentation.common.chips.GenreChip
import com.ghost.zeku.presentation.common.chips.MyChips
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.character.MediaCharacterCard
import com.ghost.zeku.presentation.components.media.character.MediaCharacterCardConfig
import com.ghost.zeku.presentation.components.media.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.toMediaPosterUiData
import com.ghost.zeku.presentation.components.media.relation.MediaRelationCard
import com.ghost.zeku.presentation.components.media.relation.MediaRelationCardConfig
import com.ghost.zeku.presentation.components.media.relation.RelationCardLayout
import com.ghost.zeku.presentation.components.media.review.ReviewCard
import com.ghost.zeku.presentation.components.section.MediaSection
import com.ghost.zeku.presentation.components.section.MediaSectionConfig
import com.ghost.zeku.presentation.components.section.PagedMediaSection
import com.ghost.zeku.presentation.navigation.Destination
import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailContract
import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailViewModel
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
    recommendations: LazyPagingItems<Media>,
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
    recommendations: LazyPagingItems<Media>,
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
                        .offset(y = -100.dp)
                        .width(config.desktopSideBarWidth)
                        .padding(
                            start = config.desktopItemSpacing,
                            end = config.desktopItemSpacing,
                            bottom = config.desktopItemSpacing
                        ),
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
                                        data = item.toMediaPosterUiData(),
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
    recommendations: LazyPagingItems<Media>,
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

        item {
            QuickInfoSection(modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.horizontalPadding))
        }

        item { SynopsisSection(state.description) }

        item { TagsSection(state.tags, onEvent) }

//        item { InfoPanel(Modifier.padding(horizontal = dimens.horizontalPadding), state, onEvent = onEvent) }

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
private fun QuickInfoSection(modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            InfoCard(title = "Status", content = "Finished")
        }
        item {
            InfoCard(title = "Episodes", content = "12")
        }
        item {
            InfoCard(title = "Start Date", content = "12 Dec 2012")
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

    val sortedTags = remember(tags) {
        tags
            .filter { !it.isSpoiler }
            .sortedByDescending { it.rank ?: 0 }
    }

    var expanded by remember { mutableStateOf(false) }
    // We still determine which tags to show, but we wrap the container in an animation
    val visibleTags = if (expanded) sortedTags else sortedTags.take(8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            // 1. This makes the height change smooth instead of jumping
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SectionHeader(
            title = "Tags",
            trailing = {
                if (sortedTags.size > 8) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Less" else "More")
                    }
                }
            }
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Added a bit of spacing for readability
        ) {
            sortedTags.forEachIndexed { index, tag ->
                // Only show the tag if it's in the first 8 OR we are expanded
                AnimatedVisibility(
                    visible = expanded || index < 8,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    TagChip(tag = tag) {
                        onEvent(MediaDetailContract.Event.OnTagClick(tag))
                    }
                }
            }
        }
    }
}


@Composable
private fun TagChip(
    tag: MediaTag,
    onClick: () -> Unit
) {
    val alpha = ((tag.rank ?: 50) / 100f).coerceIn(0.3f, 1f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha * 0.3f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelMedium
            )

            // subtle rank indicator instead of ugly text
            tag.rank?.let {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                            CircleShape
                        )
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
    trailing: (@Composable () -> Unit)? = null,
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
        trailing?.invoke()
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
    recommendations: LazyPagingItems<Media>,
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
        MediaPosterCard(data = media.toMediaPosterUiData(), modifier = mod, onAction = onAction, config = cardConfig)
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
    val colors = MaterialTheme.colorScheme

    GlassCard(modifier = Modifier, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                text = stringResource(Res.string.information),
                style = MaterialTheme.typography.titleMedium
            )

            // ---------- DETAILS ----------
            InfoSection("Details") {
                InfoRow("Status", state.status.name.format())
                InfoRow("Format", state.format.name.format())
                InfoRowIfValid("Source", state.sourceMaterial?.name?.format())
                InfoRowIfValid("Premiered", state.startDate?.format())
                InfoRowIfValid(
                    "Season",
                    state.season?.name?.format()?.plus(
                        state.seasonYear?.let { " $it" } ?: ""
                    )
                )
                InfoRowIfValid("Broadcast", state.broadcastString)
            }

            // ---------- FORMAT ----------
            InfoSection("Format") {
                when (state.type) {
                    MediaType.ANIME -> {
                        InfoRowIfValid("Episodes", state.totalEpisodes)
                        InfoRowIfValid("Duration", state.durationPerEpisode?.let { "$it min" })
                    }

                    MediaType.MANGA -> {
                        InfoRowIfValid("Volumes", state.totalVolumes)
                        InfoRowIfValid("Chapters", state.totalChapters)
                    }

                    else -> Unit
                }
            }

            // ---------- STATS ----------
            InfoSection("Stats") {
                InfoRowIfValid("Score", state.averageScore?.let { "$it%" })
                InfoRowIfValid("Mean Score", state.meanScore)
                InfoRowIfValid("Rank", state.rank?.let { "#$it" })
                InfoRowIfValid("Popularity", state.popularity?.let { "#$it" })
                InfoRowIfValid("Favorites", state.favourites)
            }

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))

            // ---------- CREDITS ----------
            CreditsSection(
                staff = state.staff,
                studios = state.studios,
                onEvent = onEvent
            )
        }
    }
}


@Composable
private fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
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
private fun CreditsSection(
    staff: List<MediaStaff>,
    studios: List<MediaStudio>,
    onEvent: (MediaDetailContract.Event) -> Unit
) {
    if (staff.isEmpty() && studios.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    val groupedStaff = remember(staff) {
        staff.groupBy { it.role }
    }

    val priorityRoles = listOf("Director", "Original Creator", "Music")

    val filteredStaff = groupedStaff.filterKeys { it in priorityRoles }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // ---------- STUDIOS ----------
        if (studios.isNotEmpty()) {
            InfoSection("Studios") {
                studios
                    .let { if (expanded) it else it.take(3) }
                    .forEach {
                        CreditRow(
                            name = it.name,
                            role = if (it.isAnimationStudio) "Animation" else "Producer",
                            onClick = {
                                onEvent(MediaDetailContract.Event.OnStudioClick(it))
                            }
                        )
                    }
            }
        }

        // ---------- STAFF ----------
        if (filteredStaff.isNotEmpty()) {
            InfoSection("Staff") {
                filteredStaff.forEach { (role, people) ->
                    people
                        .let { if (expanded) it else it.take(2) }
                        .forEach {
                            CreditRow(
                                name = it.name,
                                role = role,
                                onClick = {
                                    onEvent(MediaDetailContract.Event.OnStaffClick(it))
                                }
                            )
                        }
                }
            }
        }

        // ---------- EXPAND BUTTON ----------
        val hasMore =
            studios.size > 3 ||
                    filteredStaff.any { it.value.size > 2 }

        if (hasMore) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Show Less" else "Show More")
            }
        }
    }
}


@Composable
private fun CreditRow(
    name: String,
    role: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
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
private fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
) {
    GlassCard(
        modifier = modifier
            .widthIn(min = 100.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start // 👈 important
        ) {

            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.titleMedium, // 👈 emphasis
                maxLines = 1
            )
        }
    }
}