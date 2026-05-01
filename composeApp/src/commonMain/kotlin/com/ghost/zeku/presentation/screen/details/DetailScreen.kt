package com.ghost.zeku.presentation.screen.details


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.ghost.zeku.data.remote.anilist.model.*
import com.ghost.zeku.data.remote.anilist.toAnimeDetailsDomain
import com.ghost.zeku.data.remote.anilist.toMangaDetailsDomain
import com.ghost.zeku.data.remote.mal.model.MalAnimeDto
import com.ghost.zeku.data.remote.mal.model.MalMangaDto
import com.ghost.zeku.data.remote.mal.toAnimeDetailsDomain
import com.ghost.zeku.data.remote.mal.toMangaDetailsDomain
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.common.TrackEntry
import com.ghost.zeku.domain.model.common.format
import com.ghost.zeku.domain.model.enum.*
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.presentation.common.*
import com.ghost.zeku.presentation.common.chips.GenreChip
import com.ghost.zeku.presentation.common.chips.MyChips
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
import com.ghost.zeku.presentation.viewmodel.detail.toState
import com.ghost.zeku.utils.toPagingItems
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
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

// ---------- Previews ----------
@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_9_PRO_FOLD)
@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Preview(showBackground = true, showSystemUi = true, device = Devices.DESKTOP)
@Composable
fun PreviewMediaDetailContent() {


    val type: MediaType = MediaType.ANIME
    val provider: ProviderType = ProviderType.ANILIST

    val rawAnimeJsonMal: String = """
        {
          "id": 40748,
          "title": "Jujutsu Kaisen",
          "main_picture": {
            "medium": "https://myanimelist.net/images/anime/1171/109222.webp",
            "large": "https://myanimelist.net/images/anime/1171/109222l.webp"
          },
          "alternative_titles": {
            "synonyms": [
              "Sorcery Fight",
              "JJK"
            ],
            "en": "Jujutsu Kaisen",
            "ja": "呪術廻戦"
          },
          "start_date": "2020-10-03",
          "end_date": "2021-03-27",
          "synopsis": "Idly indulging in baseless paranormal activities with the Occult Club, high schooler Yuuji Itadori spends his days at either the clubroom or the hospital, where he visits his bedridden grandfather. However, this leisurely lifestyle soon takes a turn for the strange when he unknowingly encounters a cursed item. Triggering a chain of supernatural occurrences, Yuuji finds himself suddenly thrust into the world of Curses—dreadful beings formed from human malice and negativity—after swallowing the said item, revealed to be a finger belonging to the demon Sukuna Ryoumen, the King of Curses.\n\nYuuji experiences first-hand the threat these Curses pose to society as he discovers his own newfound powers. Introduced to the Tokyo Prefectural Jujutsu High School, he begins to walk down a path from which he cannot return—the path of a Jujutsu sorcerer.\n\n[Written by MAL Rewrite]",
          "mean": 8.51,
          "rank": 160,
          "popularity": 11,
          "num_list_users": 3027506,
          "num_scoring_users": 1995706,
          "nsfw": "white",
          "created_at": "2019-11-20T09:38:26+00:00",
          "updated_at": "2024-10-02T16:16:51+00:00",
          "media_type": "tv",
          "status": "finished_airing",
          "genres": [
            {
              "id": 1,
              "name": "Action"
            },
            {
              "id": 46,
              "name": "Award Winning"
            },
            {
              "id": 23,
              "name": "School"
            },
            {
              "id": 27,
              "name": "Shounen"
            },
            {
              "id": 37,
              "name": "Supernatural"
            }
          ],
          "my_list_status": {
            "status": "watching",
            "score": 0,
            "num_episodes_watched": 4,
            "is_rewatching": false,
            "updated_at": "2026-04-18T03:11:08+00:00",
            "start_date": "2024-11-18"
          },
          "num_episodes": 24,
          "start_season": {
            "year": 2020,
            "season": "fall"
          },
          "broadcast": {
            "day_of_the_week": "saturday",
            "start_time": "01:25"
          },
          "source": "manga",
          "average_episode_duration": 1435,
          "rating": "r",
          "pictures": [
            {
              "medium": "https://myanimelist.net/images/anime/1909/104931.jpg",
              "large": "https://myanimelist.net/images/anime/1909/104931l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1046/107701.jpg",
              "large": "https://myanimelist.net/images/anime/1046/107701l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1171/109222.jpg",
              "large": "https://myanimelist.net/images/anime/1171/109222l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1937/111424.jpg",
              "large": "https://myanimelist.net/images/anime/1937/111424l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1132/128063.jpg",
              "large": "https://myanimelist.net/images/anime/1132/128063l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1610/128064.jpg",
              "large": "https://myanimelist.net/images/anime/1610/128064l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1844/128065.jpg",
              "large": "https://myanimelist.net/images/anime/1844/128065l.jpg"
            },
            {
              "medium": "https://myanimelist.net/images/anime/1568/128066.jpg",
              "large": "https://myanimelist.net/images/anime/1568/128066l.jpg"
            }
          ],
          "background": "Winner of the Anime of the Year (TV Series) at the 2022 Tokyo Anime Award Festival (TAAF).",
          "related_anime": [
            {
              "node": {
                "id": 38777,
                "title": "Jujutsu Kaisen Official PVs",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1792/96959.webp",
                  "large": "https://myanimelist.net/images/anime/1792/96959l.webp"
                }
              },
              "relation_type": "other",
              "relation_type_formatted": "Other"
            },
            {
              "node": {
                "id": 48561,
                "title": "Jujutsu Kaisen 0 Movie",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1121/119044.jpg",
                  "large": "https://myanimelist.net/images/anime/1121/119044l.jpg"
                }
              },
              "relation_type": "prequel",
              "relation_type_formatted": "Prequel"
            },
            {
              "node": {
                "id": 51009,
                "title": "Jujutsu Kaisen 2nd Season",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1792/138022.jpg",
                  "large": "https://myanimelist.net/images/anime/1792/138022l.jpg"
                }
              },
              "relation_type": "sequel",
              "relation_type_formatted": "Sequel"
            },
            {
              "node": {
                "id": 52558,
                "title": "Vivid Vice",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1067/126222.webp",
                  "large": "https://myanimelist.net/images/anime/1067/126222l.webp"
                }
              },
              "relation_type": "other",
              "relation_type_formatted": "Other"
            },
            {
              "node": {
                "id": 56243,
                "title": "Jujutsu Kaisen 2nd Season Recaps",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1190/137716.jpg",
                  "large": "https://myanimelist.net/images/anime/1190/137716l.jpg"
                }
              },
              "relation_type": "summary",
              "relation_type_formatted": "Summary"
            }
          ],
          "related_manga": [],
          "recommendations": [
            {
              "node": {
                "id": 38000,
                "title": "Kimetsu no Yaiba",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1286/99889.webp",
                  "large": "https://myanimelist.net/images/anime/1286/99889l.webp"
                }
              },
              "num_recommendations": 73
            },
            {
              "node": {
                "id": 44511,
                "title": "Chainsaw Man",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1806/126216.webp",
                  "large": "https://myanimelist.net/images/anime/1806/126216l.webp"
                }
              },
              "num_recommendations": 50
            },
            {
              "node": {
                "id": 269,
                "title": "Bleach",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1541/147774.webp",
                  "large": "https://myanimelist.net/images/anime/1541/147774l.webp"
                }
              },
              "num_recommendations": 41
            },
            {
              "node": {
                "id": 20,
                "title": "Naruto",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1141/142503.jpg",
                  "large": "https://myanimelist.net/images/anime/1141/142503l.jpg"
                }
              },
              "num_recommendations": 32
            },
            {
              "node": {
                "id": 20507,
                "title": "Noragami",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1886/128266.webp",
                  "large": "https://myanimelist.net/images/anime/1886/128266l.webp"
                }
              },
              "num_recommendations": 17
            },
            {
              "node": {
                "id": 11061,
                "title": "Hunter x Hunter (2011)",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1337/99013.webp",
                  "large": "https://myanimelist.net/images/anime/1337/99013l.webp"
                }
              },
              "num_recommendations": 15
            },
            {
              "node": {
                "id": 1735,
                "title": "Naruto: Shippuuden",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/1565/111305.webp",
                  "large": "https://myanimelist.net/images/anime/1565/111305l.webp"
                }
              },
              "num_recommendations": 13
            },
            {
              "node": {
                "id": 9919,
                "title": "Ao no Exorcist",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/10/75195.webp",
                  "large": "https://myanimelist.net/images/anime/10/75195l.webp"
                }
              },
              "num_recommendations": 13
            },
            {
              "node": {
                "id": 31964,
                "title": "Boku no Hero Academia",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/10/78745.jpg",
                  "large": "https://myanimelist.net/images/anime/10/78745l.jpg"
                }
              },
              "num_recommendations": 12
            },
            {
              "node": {
                "id": 32182,
                "title": "Mob Psycho 100",
                "main_picture": {
                  "medium": "https://myanimelist.net/images/anime/8/80356.webp",
                  "large": "https://myanimelist.net/images/anime/8/80356l.webp"
                }
              },
              "num_recommendations": 9
            }
          ],
          "studios": [
            {
              "id": 569,
              "name": "MAPPA"
            }
          ],
          "statistics": {
            "status": {
              "watching": "365179",
              "completed": "2368148",
              "on_hold": "49783",
              "dropped": "36296",
              "plan_to_watch": "207842"
            },
            "num_list_users": 3027248
          }
        }
    """.trimIndent()
    val rawMangaJsonMal: String = """
        {
  "id": 148009,
  "title": "Level Up with the Gods",
  "main_picture": {
    "medium": "https://myanimelist.net/images/manga/2/308830.webp",
    "large": "https://myanimelist.net/images/manga/2/308830l.webp"
  },
  "alternative_titles": {
    "synonyms": [
      "Leveling with the Gods",
      "Sin-gwa Hamkke Level Up"
    ],
    "en": "Level Up with the Gods",
    "ja": "신과 함께 레벨업"
  },
  "start_date": "2021-08-14",
  "synopsis": "\"Maybe Inner Gods can never defeat Outer Gods...\" so thought Yuwon Kim, a plucky, high-ranking warrior, after a bitter defeat. But Yuwon is too tenacious to give up. His loss becomes the dawn of a new journey as a returnee. With renewed determination, Yuwon starts back from where he began, smashing monster after monster and relearning his skills before taking on the Tower once again. But can he fight his way through the tutorials and level up once more, or will his conquest crumble before him?\n\n(Source: Tapas Media)",
  "mean": 7.65,
  "rank": 2331,
  "popularity": 880,
  "num_list_users": 25056,
  "num_scoring_users": 7752,
  "nsfw": "white",
  "created_at": "1970-01-01T00:00:00+00:00",
  "updated_at": "2025-08-18T00:21:11+00:00",
  "media_type": "manhwa",
  "status": "currently_publishing",
  "genres": [
    {
      "id": 1,
      "name": "Action"
    },
    {
      "id": 2,
      "name": "Adventure"
    },
    {
      "id": 10,
      "name": "Fantasy"
    },
    {
      "id": 79,
      "name": "Time Travel"
    }
  ],
  "my_list_status": {
    "status": "reading",
    "is_rereading": false,
    "num_volumes_read": 0,
    "num_chapters_read": 167,
    "score": 9,
    "updated_at": "2026-04-29T01:16:18+00:00",
    "start_date": "2025-05-13"
  },
  "num_chapters": 0,
  "num_volumes": 0,
  "pictures": [
    {
      "medium": "https://myanimelist.net/images/manga/2/262686.jpg",
      "large": "https://myanimelist.net/images/manga/2/262686l.jpg"
    },
    {
      "medium": "https://myanimelist.net/images/manga/2/308830.jpg",
      "large": "https://myanimelist.net/images/manga/2/308830l.jpg"
    }
  ],
  "background": "Level Up with the Gods is originally a webtoon series that has been published in book format by D&C Media (디앤씨미디어) since April 29, 2024. It has been published digitally in English by Tapas Media since January 11, 2022. The volumes have been published in English by Yen Press since July 22, 2025.",
  "related_anime": [],
  "related_manga": [],
  "recommendations": [
    {
      "node": {
        "id": 121496,
        "title": "Solo Leveling",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/3/222295.webp",
          "large": "https://myanimelist.net/images/manga/3/222295l.webp"
        }
      },
      "num_recommendations": 2
    },
    {
      "node": {
        "id": 132214,
        "title": "Omniscient Reader's Viewpoint",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/2/238873.webp",
          "large": "https://myanimelist.net/images/manga/2/238873l.webp"
        }
      },
      "num_recommendations": 2
    },
    {
      "node": {
        "id": 130216,
        "title": "Tomb Raider King",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/1/239408.jpg",
          "large": "https://myanimelist.net/images/manga/1/239408l.jpg"
        }
      },
      "num_recommendations": 2
    },
    {
      "node": {
        "id": 146385,
        "title": "The Divine Twilight's Return",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/1/264059.jpg",
          "large": "https://myanimelist.net/images/manga/1/264059l.jpg"
        }
      },
      "num_recommendations": 2
    },
    {
      "node": {
        "id": 122663,
        "title": "Tower of God",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/2/223694.jpg",
          "large": "https://myanimelist.net/images/manga/2/223694l.jpg"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 132247,
        "title": "A Returner's Magic Should Be Special",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/2/239089.webp",
          "large": "https://myanimelist.net/images/manga/2/239089l.webp"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 147324,
        "title": "Second Life Ranker",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/2/261257.webp",
          "large": "https://myanimelist.net/images/manga/2/261257l.webp"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 147727,
        "title": "Overgeared",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/3/303099.jpg",
          "large": "https://myanimelist.net/images/manga/3/303099l.jpg"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 147995,
        "title": "The Player Who Can't Level Up",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/3/262642.jpg",
          "large": "https://myanimelist.net/images/manga/3/262642l.jpg"
        }
      },
      "num_recommendations": 1
    },
    {
      "node": {
        "id": 150210,
        "title": "After Ten Millennia in Hell",
        "main_picture": {
          "medium": "https://myanimelist.net/images/manga/1/266274.webp",
          "large": "https://myanimelist.net/images/manga/1/266274l.webp"
        }
      },
      "num_recommendations": 1
    }
  ],
  "authors": [
    {
      "node": {
        "id": 58860
      },
      "role": "Story"
    },
    {
      "node": {
        "id": 58861
      },
      "role": "Art"
    }
  ]
}
    """.trimIndent()
    val json = Json {
//        ignoreUnknownKeys = true // prevents crashes if extra fields exist
        isLenient = true         // allows relaxed JSON
    }

    val cachedMalAnimeDetails = json.decodeFromString<MalAnimeDto>(rawAnimeJsonMal).toAnimeDetailsDomain()
    val cachedMalMangaDetails = json.decodeFromString<MalMangaDto>(rawMangaJsonMal).toMangaDetailsDomain()

    val cachedAniListMangaDetails = AniListMedia(
        id = 138222,
        type = MediaType.MANGA.name,
        title = AniListTitle(
            romaji = "Singwa Hamkke Level Up",
            english = "Level Up with the Gods",
            native = "신과 함께 레벨업",
            userPreferred = "Level Up with the Gods"
        ),
        synonyms = listOf("Leveling With The Gods", "เลเวลอัปไปกับเทพเจ้า", "神と共にレベルアップ"),
        countryOfOrigin = "KR",
        coverImage = AniListCoverImage(
            large = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/medium/bx138222-PyVZk5MOWP07.png",
            extraLarge = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/large/bx138222-PyVZk5MOWP07.png",
            medium = null
        ),
        bannerImage = "https://s4.anilist.co/file/anilistcdn/media/manga/banner/138222-sfWXqL0VfFl9.jpg",
        description = "“Maybe Inner Gods can never defeat Outer Gods…” so thought Yu-Won Kim, a plucky, high-ranking warrior, after a bitter defeat. But Yu-Won is too tenacious to give up. His loss becomes the dawn of a new journey as a returnee. With renewed determination, Yu-Won starts back from where he began, smashing monster after monster and relearning his skills before taking on the Tower once again. But can he fight his way through the tutorials and level up once more, or will his conquest crumble before him?\n\n(Source: Tapas, edited)",
        status = MediaStatus.RELEASING.name,
        format = MediaFormat.MANGA.name,
        source = MediaSourceMaterial.NOVEL.name,
        isAdult = false,
        startDate = AniListDate(year = 2021, month = 8, day = 14),
        endDate = AniListDate(year = null, month = null, day = null),
        season = null,
        seasonYear = null,
        genres = listOf("Action", "Fantasy"),
        tags = listOf(
            AniListTag(
                name = "Gods",
                description = "Prominently features a character of divine or religious nature.",
                rank = 89,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Full Color",
                description = "Manga that were initially published in full color.",
                rank = 82,
                isMediaSpoiler = false,
                category = "Technical"
            ),
            AniListTag(
                name = "Male Protagonist",
                description = "Main character is male.",
                rank = 82,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Mythology",
                description = "Prominently features mythological elements, especially those from religious or cultural tradition.",
                rank = 81,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Time Manipulation",
                description = "Prominently features time-traveling or other time-warping phenomena.",
                rank = 78,
                isMediaSpoiler = false,
                category = "Theme-Sci-Fi"
            ),
            AniListTag(
                name = "Magic",
                description = "Prominently features magical elements or the use of magic.",
                rank = 77,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Survival",
                description = "Centers around the struggle to live in spite of extreme obstacles.",
                rank = 75,
                isMediaSpoiler = false,
                category = "Theme-Other"
            ),
            AniListTag(
                name = "Death Game",
                description = "Features characters participating in a game, where failure results in death.",
                rank = 70,
                isMediaSpoiler = false,
                category = "Theme-Other"
            ),
            AniListTag(
                name = "Age Regression",
                description = "Prominently features a character who was returned to a younger state.",
                rank = 65,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Post-Apocalyptic",
                description = "Partly or completely set in a world or civilization after a global disaster.",
                rank = 62,
                isMediaSpoiler = false,
                category = "Setting-Universe"
            ),
            AniListTag(
                name = "Long Strip",
                description = "Manga originally published in a vertical, long-strip format, designed for viewing on smartphones. Also known as webtoons.",
                rank = 20,
                isMediaSpoiler = false,
                category = "Technical"
            )
        ),
        averageScore = 78,
        meanScore = 78,
        popularity = 26812,
        favourites = 1043,
        episodes = null,
        duration = null,
        chapters = null,
        volumes = null,
        nextAiringEpisode = null,
        studios = null,
        trailer = AniListTrailer(
            id = "xhObyLPUq6w",
            site = "Youtube",
            thumbnail = "https://i.ytimg.com/vi/xhObyLPUq6w/hqdefault.jpg"
        ),
        externalLinks = listOf(
            AniListExternalLink(
                url = "https://page.kakao.com/home?seriesId=57311327",
                site = "KakaoPage",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/55-Q8bDHOAd7vBl.png"
            ),
            AniListExternalLink(
                url = "https://tapas.io/series/level-up-with-the-gods/info",
                site = "Tapas",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/75-RqhaL2l9Eya2.png"
            ),
            AniListExternalLink(
                url = "https://piccoma.com/web/product/82357",
                site = "Piccoma",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/63-6UOHPZ06XAp7.png"
            ),
            AniListExternalLink(
                url = "https://webtoon.kakao.com/content/신과-함께-레벨업/2708",
                site = "Kakao Webtoon",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/137-wC5kGLPmxvKz.png"
            ),
            AniListExternalLink(
                url = "https://yenpress.com/series/level-up-with-the-gods",
                site = "Yen Press",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/183-l4ZkVEmBY5Af.png"
            )
        ),
        characters = AniListCharacterConnection(
            edges = listOf(
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 266306,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b266306-TelgD3HC5EhO.jpg")
                    )
                )
            )
        ),
        relations = AniListRelationConnection(edges = emptyList()),
        staff = AniListStaffConnection(
            edges = listOf(
                AniListStaffEdge(
                    role = "Original Story",
                    node = AniListStaffNode(
                        id = 170305,
                        name = AniListTitle(romaji = null, english = null, native = null, userPreferred = "Heugain"),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/default.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Story & Art",
                    node = AniListStaffNode(
                        id = 245252,
                        name = AniListTitle(romaji = null, english = null, native = null, userPreferred = "O-Hyeon"),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n245252-eFkNz54VSTPf.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Editing (French)",
                    node = AniListStaffNode(
                        id = 368010,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Douglas De Almeida"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/default.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                )
            )
        ),
        mediaListEntry = AniListMediaListEntry(
            id = 441278851,
            mediaId = null,
            status = TrackStatus.CURRENT.name,
            progress = 167,
            score = 8.9,
            media = null
        )
    ).toMangaDetailsDomain()
    val cachedAniListAnimeDetails = AniListMedia(
        id = 113415,
        type = "ANIME", // Assuming an Enum
        title = AniListTitle(
            romaji = "Jujutsu Kaisen",
            english = "JUJUTSU KAISEN",
            native = "呪術廻戦",
            userPreferred = "JUJUTSU KAISEN"
        ),
        synonyms = listOf(
            "JJK",
            "Sorcery Fight",
            "咒术回战",
            "주술회전",
            "มหาเวทย์ผนึกมาร",
            "جوجوتسو كايسن",
            "Магическая битва",
            "咒術迴戰"
        ),
        countryOfOrigin = "JP",
        coverImage = AniListCoverImage(
            large = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx113415-LHBAeoZDIsnF.jpg",
            extraLarge = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx113415-LHBAeoZDIsnF.jpg",
            medium = null
        ),
        bannerImage = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/113415-jQBSkxWAAk83.jpg",
        description = "A boy fights... for \"the right death.\"\n\nHardship, regret, shame: the negative feelings that humans feel become Curses that lurk in our everyday lives...",
        status = MediaStatus.FINISHED.name,
        format = MediaFormat.TV.name,
        source = MediaSourceMaterial.MANGA.name,
        isAdult = false,
        startDate = AniListDate(year = 2020, month = 10, day = 3),
        endDate = AniListDate(year = 2021, month = 3, day = 27),
        season = MediaSeason.FALL.name,
        seasonYear = 2020,
        genres = listOf("Action", "Drama", "Supernatural"),
        tags = listOf(
            AniListTag(
                name = "Urban Fantasy",
                description = "Set in a world similar to the real world...",
                rank = 93,
                isMediaSpoiler = false,
                category = "Setting-Universe"
            ),
            AniListTag(
                name = "Shounen",
                description = "Target demographic is teenage and young adult males.",
                rank = 92,
                isMediaSpoiler = false,
                category = "Demographic"
            ),
            AniListTag(
                name = "Curses",
                description = "Features a character, object or area that has been cursed...",
                rank = 89,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Super Power",
                description = "Prominently features characters with special abilities...",
                rank = 88,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Youkai",
                description = "Prominently features supernatural creatures from Japanese folklore.",
                rank = 87,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Male Protagonist",
                description = "Main character is male.",
                rank = 85,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Primarily Teen Cast",
                description = "Main cast is mostly composed of teen characters.",
                rank = 84,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Exorcism",
                description = "Involving religious methods of vanquishing youkai...",
                rank = 83,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Demons",
                description = "Prominently features malevolent otherworldly creatures.",
                rank = 80,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Orphan",
                description = "Prominently features a character that is an orphan.",
                rank = 80,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Dissociative Identities",
                description = "A case where one or more people share the same body.",
                rank = 80,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Urban",
                description = "Partly or completely set in a city.",
                rank = 79,
                isMediaSpoiler = false,
                category = "Setting-Scene"
            ),
            AniListTag(
                name = "Body Horror",
                description = "Features characters who undergo horrific transformations...",
                rank = 75,
                isMediaSpoiler = false,
                category = "Theme-Other"
            ),
            AniListTag(
                name = "Primarily Male Cast",
                description = "Main cast is mostly composed of male characters.",
                rank = 72,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Magic",
                description = "Prominently features magical elements...",
                rank = 71,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Martial Arts",
                description = "Centers around the use of traditional hand-to-hand combat.",
                rank = 70,
                isMediaSpoiler = false,
                category = "Theme-Action"
            ),
            AniListTag(
                name = "Shapeshifting",
                description = "Features character(s) who changes one's appearance or form.",
                rank = 68,
                isMediaSpoiler = true,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Ensemble Cast",
                description = "Features a large cast of characters...",
                rank = 68,
                isMediaSpoiler = false,
                category = "Cast-Main Cast"
            ),
            AniListTag(
                name = "Gore",
                description = "Prominently features graphic bloodshed and violence.",
                rank = 64,
                isMediaSpoiler = false,
                category = "Theme-Other"
            ),
            AniListTag(
                name = "Anthropomorphism",
                description = "Contains non-human character(s)...",
                rank = 60,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Swordplay",
                description = "Prominently features the use of swords in combat.",
                rank = 60,
                isMediaSpoiler = false,
                category = "Theme-Action"
            ),
            AniListTag(
                name = "School",
                description = "Partly or completely set in a primary or secondary educational institution.",
                rank = 59,
                isMediaSpoiler = false,
                category = "Setting-Scene"
            ),
            AniListTag(
                name = "Rotoscoping",
                description = "Animation technique...",
                rank = 54,
                isMediaSpoiler = false,
                category = "Technical"
            ),
            AniListTag(
                name = "Slapstick",
                description = "Prominently features comedy based on deliberately clumsy actions...",
                rank = 51,
                isMediaSpoiler = false,
                category = "Theme-Comedy"
            ),
            AniListTag(
                name = "Mythology",
                description = "Prominently features mythological elements...",
                rank = 50,
                isMediaSpoiler = false,
                category = "Theme-Fantasy"
            ),
            AniListTag(
                name = "Boarding School",
                description = "Features characters attending a boarding school.",
                rank = 50,
                isMediaSpoiler = false,
                category = "Setting-Scene"
            ),
            AniListTag(
                name = "Archery",
                description = "Centers around the sport of archery...",
                rank = 50,
                isMediaSpoiler = false,
                category = "Theme-Action"
            ),
            AniListTag(
                name = "Twins",
                description = "Prominently features two or more siblings...",
                rank = 48,
                isMediaSpoiler = false,
                category = "Cast-Traits"
            ),
            AniListTag(
                name = "Tragedy",
                description = "Centers around tragic events and unhappy endings.",
                rank = 46,
                isMediaSpoiler = true,
                category = "Theme-Drama"
            ),
            AniListTag(
                name = "Surreal Comedy",
                description = "Prominently features comedic moments that defy casual reasoning...",
                rank = 45,
                isMediaSpoiler = false,
                category = "Theme-Comedy"
            ),
            AniListTag(
                name = "Baseball",
                description = "Centers around the sport of baseball.",
                rank = 35,
                isMediaSpoiler = false,
                category = "Theme-Game-Sport"
            )
        ),
        averageScore = 84,
        meanScore = 84,
        popularity = 896031,
        favourites = 63195,
        episodes = 24,
        duration = 24,
        chapters = null,
        volumes = null,
        nextAiringEpisode = null,
        studios = AniListStudioConnection(
            edges = listOf(
                AniListStudioEdge(
                    isMain = false,
                    node = AniListStudioNode(id = 245, name = "Toho", isAnimationStudio = false)
                ),
                AniListStudioEdge(
                    isMain = true,
                    node = AniListStudioNode(id = 569, name = "MAPPA", isAnimationStudio = true)
                ),
                AniListStudioEdge(
                    isMain = false,
                    node = AniListStudioNode(id = 6570, name = "Shueisha", isAnimationStudio = false)
                ),
                AniListStudioEdge(
                    isMain = false,
                    node = AniListStudioNode(id = 6602, name = "Sumzap", isAnimationStudio = false)
                ),
                AniListStudioEdge(
                    isMain = false,
                    node = AniListStudioNode(id = 143, name = "Mainichi Broadcasting System", isAnimationStudio = false)
                )
            )
        ),
        trailer = AniListTrailer(
            id = "pkKu9hLT-t8",
            site = "YOUTUBE",
            thumbnail = "https://i.ytimg.com/vi/pkKu9hLT-t8/hqdefault.jpg"
        ),
        externalLinks = listOf(
            AniListExternalLink(url = "https://jujutsukaisen.jp/", site = "Official Site", icon = null),
            AniListExternalLink(
                url = "https://twitter.com/animejujutsu",
                site = "Twitter",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/17-R0tMgOvwvhsS.png"
            ),
            AniListExternalLink(
                url = "https://www.crunchyroll.com/jujutsu-kaisen",
                site = "Crunchyroll",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/5-AWN2pVlluCOO.png"
            ),
            AniListExternalLink(
                url = "https://www.netflix.com/title/81278456",
                site = "Netflix",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/10-rVGPom8RCiwH.png"
            ),
            AniListExternalLink(
                url = "https://www.iq.com/album/igc33vhvex",
                site = "iQ",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/122-EPBJ2E0oPt5C.png"
            ),
            AniListExternalLink(
                url = "https://www.bilibili.tv/media/37738",
                site = "Bilibili TV",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/119-NCwGvCjFADGQ.png"
            ),
            AniListExternalLink(
                url = "https://twitter.com/Jujutsu_anime",
                site = "Twitter",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/17-R0tMgOvwvhsS.png"
            ),
            AniListExternalLink(
                url = "https://youtube.com/playlist?list=PLxSscENEp7JisDU6GAJuyNpVwDvCm-f3J",
                site = "YouTube",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/13-ZwR1Xwgtyrwa.png"
            ),
            AniListExternalLink(
                url = "https://twitter.com/Jujutsu_Kaisen_",
                site = "Twitter",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/17-R0tMgOvwvhsS.png"
            ),
            AniListExternalLink(
                url = "https://www.hulu.com/series/jujutsu-kaisen-382ec8bf-3650-4cde-94db-ecd18665f9e0",
                site = "Hulu",
                icon = "https://s4.anilist.co/file/anilistcdn/link/icon/7-rM06PQyWONGC.png"
            )
        ),
        characters = AniListCharacterConnection(
            edges = listOf(
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 126635,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b126635-L0y3I92JSUkN.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 127212,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b127212-FVm2tD0erQ5B.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 133700,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b133700-f6sOO3TcgLV6.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.MAIN.name,
                    node = AniListCharacterNode(
                        id = 127691,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b127691-9zqh1xpIubn7.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 133704,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b133704-8wLTGjc234q2.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 157867,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b157867-dHdd8ZECuzHx.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 200767,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b200767-bTqPLS2Jpiqf.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 209694,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b209694-xSS6FxN4al3l.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 172743,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b172743-4Y5SXqED6A3G.jpg")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 189237,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b189237-ZxNJwVPL8DvW.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 200768,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b200768-fgIRyfO8ABk2.jpg")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 157865,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b157865-X9ENX9OzWevS.jpg")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 210846,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b210846-XcRQ643Ne8Pb.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 133702,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b133702-Y7JRG5vAvjIL.png")
                    )
                ),
                AniListCharacterEdge(
                    role = CharacterRole.SUPPORTING.name,
                    node = AniListCharacterNode(
                        id = 158154,
                        name = AniListCharacterName(full = null),
                        image = AniListCharacterImage(large = "https://s4.anilist.co/file/anilistcdn/character/large/b158154-UCqbiULli62P.png")
                    )
                )
            )
        ),
        relations = AniListRelationConnection(
            edges = listOf(
                AniListRelationEdge(
                    relationType = RelationType.ADAPTATION.name,
                    node = AniListMedia(
                        id = 101517,
                        type = MediaType.MANGA.name,
                        title = AniListTitle(
                            romaji = "Jujutsu Kaisen",
                            english = "Jujutsu Kaisen",
                            native = "呪術廻戦",
                            userPreferred = "Jujutsu Kaisen"
                        ),
                        synonyms = null,
                        countryOfOrigin = null,
                        coverImage = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/medium/bx101517-H3TdM3g5ZUe9.jpg",
                            extraLarge = null,
                            medium = null
                        ),
                        bannerImage = null,
                        description = null,
                        status = MediaStatus.FINISHED.name,
                        format = MediaFormat.MANGA.name,
                        source = null,
                        isAdult = null,
                        startDate = null,
                        endDate = null,
                        season = null,
                        seasonYear = null,
                        genres = null,
                        tags = null,
                        averageScore = 80,
                        meanScore = null,
                        popularity = null,
                        favourites = null,
                        episodes = null,
                        duration = null,
                        chapters = 272,
                        volumes = null,
                        nextAiringEpisode = null,
                        studios = null,
                        trailer = null,
                        externalLinks = null,
                        characters = null,
                        relations = null,
                        staff = null,
                        mediaListEntry = null
                    )
                ),
                AniListRelationEdge(
                    relationType = RelationType.PREQUEL.name,
                    node = AniListMedia(
                        id = 131573,
                        type = MediaType.ANIME.name,
                        title = AniListTitle(
                            romaji = "Jujutsu Kaisen 0",
                            english = "JUJUTSU KAISEN 0",
                            native = "呪術廻戦 0",
                            userPreferred = "JUJUTSU KAISEN 0"
                        ),
                        synonyms = null,
                        countryOfOrigin = null,
                        coverImage = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx131573-rpl82vDEDRm6.jpg",
                            extraLarge = null,
                            medium = null
                        ),
                        bannerImage = null,
                        description = null,
                        status = MediaStatus.FINISHED.name,
                        format = MediaFormat.MOVIE.name,
                        source = null,
                        isAdult = null,
                        startDate = null,
                        endDate = null,
                        season = null,
                        seasonYear = null,
                        genres = null,
                        tags = null,
                        averageScore = 83,
                        meanScore = null,
                        popularity = null,
                        favourites = null,
                        episodes = 1,
                        duration = null,
                        chapters = null,
                        volumes = null,
                        nextAiringEpisode = null,
                        studios = null,
                        trailer = null,
                        externalLinks = null,
                        characters = null,
                        relations = null,
                        staff = null,
                        mediaListEntry = null
                    )
                ),
                AniListRelationEdge(
                    relationType = RelationType.SEQUEL.name,
                    node = AniListMedia(
                        id = 145064,
                        type = MediaType.ANIME.name,
                        title = AniListTitle(
                            romaji = "Jujutsu Kaisen 2nd Season",
                            english = "JUJUTSU KAISEN Season 2",
                            native = "呪術廻戦 第2期",
                            userPreferred = "JUJUTSU KAISEN Season 2"
                        ),
                        synonyms = null,
                        countryOfOrigin = null,
                        coverImage = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx145064-hSNRJM03pvv1.jpg",
                            extraLarge = null,
                            medium = null
                        ),
                        bannerImage = null,
                        description = null,
                        status = MediaStatus.FINISHED.name,
                        format = MediaFormat.TV.name,
                        source = null,
                        isAdult = null,
                        startDate = null,
                        endDate = null,
                        season = null,
                        seasonYear = null,
                        genres = null,
                        tags = null,
                        averageScore = 86,
                        meanScore = null,
                        popularity = null,
                        favourites = null,
                        episodes = 23,
                        duration = null,
                        chapters = null,
                        volumes = null,
                        nextAiringEpisode = null,
                        studios = null,
                        trailer = null,
                        externalLinks = null,
                        characters = null,
                        relations = null,
                        staff = null,
                        mediaListEntry = null
                    )
                ),
                AniListRelationEdge(
                    relationType = RelationType.OTHER.name,
                    node = AniListMedia(
                        id = 147463,
                        type = MediaType.ANIME.name,
                        title = AniListTitle(
                            romaji = "Jujutsu Kaisen PV",
                            english = null,
                            native = "呪術廻戦PV",
                            userPreferred = "Jujutsu Kaisen PV"
                        ),
                        synonyms = null,
                        countryOfOrigin = null,
                        coverImage = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/b147463-LmoLwL9DbYAn.jpg",
                            extraLarge = null,
                            medium = null
                        ),
                        bannerImage = null,
                        description = null,
                        status = MediaStatus.FINISHED.name,
                        format = MediaFormat.ONA.name,
                        source = null,
                        isAdult = null,
                        startDate = null,
                        endDate = null,
                        season = null,
                        seasonYear = null,
                        genres = null,
                        tags = null,
                        averageScore = 67,
                        meanScore = null,
                        popularity = null,
                        favourites = null,
                        episodes = 3,
                        duration = null,
                        chapters = null,
                        volumes = null,
                        nextAiringEpisode = null,
                        studios = null,
                        trailer = null,
                        externalLinks = null,
                        characters = null,
                        relations = null,
                        staff = null,
                        mediaListEntry = null
                    )
                )
            )
        ),
        staff = AniListStaffConnection(
            edges = listOf(
                AniListStaffEdge(
                    role = "ADR Director (English)",
                    node = AniListStaffNode(
                        id = 95512,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Michael Sorich"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n95512-ZF7RSh28tB0l.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Series Composition",
                    node = AniListStaffNode(
                        id = 99012,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroshi Seko"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n99012-H3OCMnoTqmEV.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Script (eps 1-24)",
                    node = AniListStaffNode(
                        id = 99012,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroshi Seko"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n99012-H3OCMnoTqmEV.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "ADR Director (Latin American Spanish)",
                    node = AniListStaffNode(
                        id = 100029,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Patricia Acevedo"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/5029.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (eps 4, 5)",
                    node = AniListStaffNode(
                        id = 100812,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Yoshiaki Kawajiri"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n100812-1ZrgDInanSvE.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (ep 1)",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Core Director (eps 2, 3, 6)",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Juju Sanpo Chief Animation Director (eps 13, 15)",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Character Design",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Chief Animation Director (OP1, OP2, ep 1)",
                    node = AniListStaffNode(
                        id = 101185,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Tadashi Hiramatsu"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n101185-CJLT9YKxSDZC.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Art Director (OP1, OP2)",
                    node = AniListStaffNode(
                        id = 103102,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Yuusuke Takeda"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103102-aEjIrYuaUaVn.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Script (German)",
                    node = AniListStaffNode(
                        id = 103757,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "René Dawn-Claude"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/8757.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "ADR Director (German)",
                    node = AniListStaffNode(
                        id = 103757,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "René Dawn-Claude"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/8757.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (ep 13)",
                    node = AniListStaffNode(
                        id = 103979,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hironori Tanaka"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103979-iNgo68ikjKgx.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Episode Director (ep 13)",
                    node = AniListStaffNode(
                        id = 103979,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hironori Tanaka"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103979-iNgo68ikjKgx.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Animation Director (ep 13)",
                    node = AniListStaffNode(
                        id = 103979,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hironori Tanaka"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103979-iNgo68ikjKgx.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Key Animation (eps 4, 13, 20, 24)",
                    node = AniListStaffNode(
                        id = 103979,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hironori Tanaka"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n103979-iNgo68ikjKgx.jpg",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Key Animation (ep 6)",
                    node = AniListStaffNode(
                        id = 104457,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Toshiya Washida"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n104457-2IKwEQWGcwLm.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Cursed Spirit Design (OP1, eps 1-24)",
                    node = AniListStaffNode(
                        id = 106114,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroya Iijima"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n106114-WqnSCKNql5TU.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Key Animation (ep 12)",
                    node = AniListStaffNode(
                        id = 106114,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroya Iijima"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n106114-WqnSCKNql5TU.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Prop Design (eps 3-24)",
                    node = AniListStaffNode(
                        id = 106114,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroya Iijima"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n106114-WqnSCKNql5TU.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (ep 3)",
                    node = AniListStaffNode(
                        id = 106475,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Hiroaki Andou"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n106475-kUCzyMZZKBfu.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Storyboard (eps 12, 18, 20)",
                    node = AniListStaffNode(
                        id = 107199,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Fuminori Kizaki"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n107199-4je9Gs4pEXju.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Key Animation (eps 13, 16, 23)",
                    node = AniListStaffNode(
                        id = 107357,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Toshiyuki Satou"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n107357-96PgjYcdwBFt.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                ),
                AniListStaffEdge(
                    role = "Chief Animation Director (eps 4, 8, 13, 16, 19, 23)",
                    node = AniListStaffNode(
                        id = 107584,
                        name = AniListTitle(
                            romaji = null,
                            english = null,
                            native = null,
                            userPreferred = "Terumi Nishii"
                        ),
                        image = AniListCoverImage(
                            large = "https://s4.anilist.co/file/anilistcdn/staff/large/n107584-CPAvAAk43Ih0.png",
                            extraLarge = null,
                            medium = null
                        )
                    )
                )
            )
        ),
        mediaListEntry = AniListMediaListEntry(
            id = 442863659,
            mediaId = null,
            status = TrackStatus.CURRENT.name,
            progress = 4,
            score = 0.0,
            media = null
        )
    ).toAnimeDetailsDomain()


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


    val state = when (provider) {
        ProviderType.ANILIST -> {
            when (type) {
                MediaType.ANIME -> cachedAniListAnimeDetails.toState()
                MediaType.MANGA -> cachedAniListMangaDetails.toState()
                MediaType.UNKNOWN -> cachedAniListMangaDetails.toState()
            }
        }

        ProviderType.MYANIMELIST -> {
            when (type) {
                MediaType.ANIME -> cachedMalAnimeDetails.toState()
                MediaType.MANGA -> cachedMalMangaDetails.toState()
                MediaType.UNKNOWN -> cachedMalMangaDetails.toState()
            }
        }
    }


    val isWideScreen = rememberPlatformConfiguration().isWideScreen

    AppTheme {
        MediaDetailContent(
            state = state,
            episodes = emptyList<Episode>().toPagingItems(),
            recommendations = emptyList<Anime>().toPagingItems(),
            reviews = emptyList<Review>().toPagingItems(),
            onEvent = {},
            config = MediaDetailUiConfig(),
            isWideScreen = isWideScreen
        )
    }
}