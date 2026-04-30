package com.ghost.zeku.presentation.screen.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import com.ghost.zeku.presentation.viewmodel.detail.*
import com.ghost.zeku.utils.formatTimestamp
import com.ghost.zeku.utils.toPagingItems
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import zeku.composeapp.generated.resources.*

@Composable
fun MediaDetailScreen(
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
        viewModel.onEvent(MediaDetailContract.Event.Load(mediaId, mediaType))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {

                is MediaDetailContract.Effect.Navigate -> {
                    onNavigate(effect.destination)
                }

                is MediaDetailContract.Effect.OpenExternalLink -> {
                    TODO("Not yet implemented")
                }

                is MediaDetailContract.Effect.PlayTrailer -> {
                    TODO("Not yet implemented")
                }

                is MediaDetailContract.Effect.ShowMessage -> {
                    TODO()
                }

                is MediaDetailContract.Effect.OpenChat -> {
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
                onClick = { /* chat */ },
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
                            onAction = {
                                onEvent(MediaDetailContract.Event.ViewAllReviews(mediaId = mediaId))
                            }
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
                            onClick = { onEvent(MediaDetailContract.Event.OnExternalLinkClick(it)) }
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
                                config = config.relationCard.copy(
                                    layout = RelationCardLayout.WIDE
                                ),
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                            ) {
                                row.forEach { item ->
                                    MediaPosterCard(
                                        data = item.toPosterUiData(),
                                        modifier = Modifier.weight(1f),
                                        onAction = { onEvent(MediaDetailContract.Event.OnMediaAction(it)) },
                                        config = config.recommendationCard
                                    )
                                }
                                if (row.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
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
                    val overlap = config.androidHeroMetaOffset.roundToPx() // fixed: now uses config
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
                onClick = { /* TODO */ },
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

        item {
            InfoPanel(Modifier.padding(horizontal = dimens.horizontalPadding), state, onEvent = onEvent)
        }
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
                    onClick = { onEvent(MediaDetailContract.Event.OnExternalLinkClick(it)) }
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
                onAction = {
                    onEvent(MediaDetailContract.Event.ViewAllReviews(mediaId = mediaId))
                }

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
            GenreChip(onClick = { onEvent(MediaDetailContract.Event.OnGenreClick(genre)) }, genre)
        }
    }
    Spacer(Modifier.height(dimens.spacingSmall))

    Text(
        text = state.title,
        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(Modifier.height(dimens.spacingSmall))

    // Rating, Release date, Studio/Author
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(dimens.iconSize)
        )
        Text(
            text = " ${state.rating}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            stringResource(Res.string.dot_separator),
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = state.releaseDate?.let { formatTimestamp(it) } ?: stringResource(Res.string.unknown),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        )
        Text(
            stringResource(Res.string.dot_separator),
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = state.studio ?: state.author ?: stringResource(Res.string.unknown),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        )
    }

    if (isDesktop) {
        Spacer(Modifier.height(dimens.spacingMedium))
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium)) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                Text(stringResource(Res.string.watch_now), color = MaterialTheme.colorScheme.onSecondary)
            }
            OutlinedButton(
                onClick = {},
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
private fun SynopsisSection(
    synopsis: String?
) {
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

        // Header with actions
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = stringResource(Res.string.synopsis),
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {

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

        // Content with fade edge
        Box {

            SelectionContainer {
                Text(
                    text = text,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Gradient fade when collapsed
            if (!expanded && text.length > 150) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                ),
                                startY = 100f
                            )
                        )
                )
            }
        }

        // Expand / Collapse CTA
        if (text.length > 150) {
            Text(
                text = if (expanded) "Show less" else "Read more",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clickable { expanded = !expanded }
            )
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
                        onEvent(MediaDetailContract.Event.PlayTrailer(it))
                    }
                }
        ) {

            // 🎞️ Thumbnail
            MediaAsyncImage(
                url = trailer?.thumbnail ?: "",
                contentDescription = stringResource(Res.string.official_trailer),
                modifier = Modifier.fillMaxSize(),
            )

            // 🌈 Gradient overlay (better than flat black)
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

            // ▶️ Play Button (center)
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

            // 📝 Bottom Info
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
    val dimens = MediaDetailDimens
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


// ---------- Character Section ----------
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
    ) { character, modifier ->
        MediaCharacterCard(
            character = character,
            config = cardConfig,
            modifier = modifier,
            onClick = onCharacterClick
        )
    }
}

// ---------- Relation Section ----------
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
        items = relations.take(1), // Keep original logic, might need full list elsewhere
        onViewAllClick = onViewAllClick,
        config = sectionConfig,
        key = null,
        isLoading = false,
        modifier = modifier
    ) { relation, modifier ->
        MediaRelationCard(
            relation = relation,
            config = cardConfig,
            modifier = modifier,
            onClick = onRelationClick
        )
    }
}


// ---------- Recommendation Section ----------
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
    ) { media, modifier ->
        MediaPosterCard(
            data = media.toPosterUiData(),
            modifier = modifier,
            onAction = onAction,
            config = cardConfig
        )
    }
}

@Composable
private fun ExternalLinksPanel(
    links: List<ExternalLink>,
    isMobile: Boolean = false,
    onClick: (ExternalLink) -> Unit
) {
    val dimens = MediaDetailDimens

    // ✅ 1. Empty state guard
    val safeLinks = remember(links) {
        links
            .filter { it.url.isNotBlank() }
            .distinctBy { it.url } // avoid duplicates
    }

    if (safeLinks.isEmpty()) return

    val title = stringResource(Res.string.watch_follow)

    if (isMobile) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.horizontalPadding),
        ) {
            SectionHeader(title = title)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
            ) {
                items(
                    items = safeLinks,
                    key = { it.url } // ✅ stable key
                ) { link ->
                    ExternalLinkItem(link = link, onClick = onClick)
                }
            }
        }
    } else {
        GlassCard(
            shape = RoundedCornerShape(MediaDetailDimens.glassCardCornerRadius)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = dimens.spacingMedium, vertical = dimens.verticalPadding)
            ) {
                SectionHeader(title = title)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
                ) {
                    safeLinks.forEach { link ->
                        ExternalLinkItem(link = link, onClick = onClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExternalLinkItem(
    link: ExternalLink,
    onClick: (ExternalLink) -> Unit
) {
    val fallbackIcon = remember(link.site) {
        // Simple fallback based on name
        when {
            link.site.contains("twitter", true) -> Icons.Default.Share
            link.site.contains("youtube", true) -> Icons.Default.PlayArrow
            link.site.contains("crunchyroll", true) -> Icons.Default.PlayArrow
            else -> Icons.Default.Link
        }
    }


    MyChips(
        onClick = {
            if (link.url.isNotBlank()) {
                onClick(link)
            }
        },
        shape = RoundedCornerShape(percent = 25),
        leadingIcon = {

            // ✅ 1. If no icon URL → fallback immediately
            if (link.iconUrl.isNullOrBlank()) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = link.site,
                    modifier = Modifier.size(18.dp)
                )
                return@MyChips
            }

            // ✅ 2. Async image with loading + error fallback
            AsyncImage(
                model = link.iconUrl,
                contentDescription = link.site,

                modifier = Modifier.size(18.dp),

                // ✅ Placeholder while loading
                placeholder = rememberVectorPainter(Icons.Default.Web),

                // ✅ If fails → fallback icon
                error = rememberVectorPainter(Icons.Default.BrokenImage),
                fallback = rememberVectorPainter(Icons.Default.Link),

                contentScale = ContentScale.Fit
            )
        },
        text = {
            Text(
                text = link.site.ifBlank { stringResource(Res.string.open_link) }, // ✅ fallback text
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
        shape = RoundedCornerShape(MediaDetailDimens.glassCardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMedium)
        ) {

            // -------------------------
            // HEADER
            // -------------------------
            Text(
                text = stringResource(Res.string.information),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )

            // -------------------------
            // CORE INFO
            // -------------------------
            InfoGroup {
                InfoRow(stringResource(Res.string.status), state.status.name)
                InfoRow(stringResource(Res.string.format), state.format.name)

                InfoRow(
                    stringResource(Res.string.premiered),
                    state.releaseDate?.let { formatTimestamp(it) }
                        ?: stringResource(Res.string.unknown)
                )
            }

            // -------------------------
            // TYPE SPECIFIC
            // -------------------------
            InfoGroup {
                when (state.type) {
                    MediaType.ANIME -> {
                        InfoRowIfValid(
                            stringResource(Res.string.episodes),
                            state.totalEpisodes
                        )
                        InfoRowIfValid(
                            stringResource(Res.string.duration),
                            state.episodeDuration?.let { "$it min" }
                        )
                    }

                    MediaType.MANGA -> {
                        InfoRowIfValid(
                            stringResource(Res.string.volumes),
                            state.totalVolumes
                        )
                        InfoRowIfValid(
                            stringResource(Res.string.chapters),
                            state.totalChapters
                        )
                    }

                    else -> {}
                }

                InfoRowIfValid(
                    stringResource(Res.string.score),
                    state.rating.takeIf { it != null && it > 0 }?.toString()
                )
            }

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))

            // -------------------------
            // CREDITS
            // -------------------------
            val credits = listOfNotNull(
                state.artist?.let { CreditType.Artist(it) },
                state.author?.let { CreditType.Author(it) },
                state.studio?.let { CreditType.Studio(it) }
            )

            if (credits.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSmall)
                ) {

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
                                onClick = { onEvent(credit.toEvent()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

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
fun CreditChip(
    credit: CreditType,
    onClick: () -> Unit
) {
    MyChips(
        onClick = onClick,
        text = {
            Column {
                Text(
                    text = credit.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (credit) {
                        is CreditType.Artist -> credit.name
                        is CreditType.Author -> credit.name
                        is CreditType.Studio -> credit.name
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        leadingIcon = {
            Icon(
                credit.icon,
                contentDescription = credit.label
            )
        }
    )
}

@Composable
private fun InfoRowIfValid(
    label: String,
    value: Any?
) {
    val text = when (value) {
        null -> null
        is String -> value.takeIf { it.isNotBlank() }
        is Number -> value.toString()
        else -> value.toString()
    }

    if (text != null) {
        InfoRow(label, text)
    }
}

// ---------- Glass Card ----------


// ---------- Previews ----------
@Preview(showBackground = true, widthDp = 450)
@Composable
fun PreviewMediaDetailContent() {
    val state = MediaDetailContract.State(
        id = 1,
        type = MediaType.ANIME,
        source = ProviderType.MYANIMELIST,
        title = "Jujutsu Kaisen",
        description = """
            Although Yuji Itadori looks like your average teenager, his immense physical strength is something to behold! Every sports club wants him to join, but Itadori would rather hang out with the school outcasts in the Occult Research Club. One day, the club manages to get their hands on a sealed cursed object. Little do they know the terror they’ll unleash when they break the seal…

            (Source: VIZ Media)

            Notes:
            - Ranked 1st in Japan's Bookstore Employees Top Manga of 2018.
            - Nominated for the 25th Annual Tezuka Osamu Cultural Prize in 2021.
            - Nominated for the 65th Shogakukan Manga Award in the Shounen Category in 2019.
            - Includes one extra chapter.
        """.trimIndent(),
        coverImage = "https://www.themoviedb.org/t/p/w1280/fHpKWq9ayzSk8nSwqRuaAUemRKh.jpg",
        bannerImage = "https://media.themoviedb.org/t/p/w1066_and_h600_face/gmECX1DvFgdUPjtio2zaL8BPYPu.jpg",
        genres = listOf("Action", "Supernatural", "Shounen"),
        rating = 8.7,
        characters = listOf(
            MediaCharacter(
                1,
                "Yuji Itadori",
                "https://s4.anilist.co/file/anilistcdn/character/large/b127212-FVm2tD0erQ5B.png",
                CharacterRole.MAIN
            ),
            MediaCharacter(
                2,
                "Gojo Satoru",
                "https://s4.anilist.co/file/anilistcdn/character/large/b127691-9zqh1xpIubn7.png",
                CharacterRole.MAIN
            ),
            MediaCharacter(
                3,
                "Megumi Fushiguro",
                "https://s4.anilist.co/file/anilistcdn/character/large/b126635-L0y3I92JSUkN.png",
                CharacterRole.SUPPORTING
            )
        ),
        relations = listOf(
            MediaRelation(
                10,
                RelationType.SEQUEL,
                MediaTitle(romaji = "Jujutsu Kaisen 0", null, null),
                "https://myanimelist.net/images/manga/1/157897l.webp",
                MediaType.ANIME,
                MediaFormat.MOVIE
            ),
            MediaRelation(
                11,
                RelationType.PREQUEL,
                MediaTitle(romaji = "Jujutsu Kaisen: Final", null, null),
                "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx101922-WBsBl0ClmgYL.jpg",
                MediaType.ANIME,
                MediaFormat.MOVIE
            )
        ),
        trailer = MediaTrailer(
            id = "xdsss",
            site = "Crunchy Role",
            thumbnail = "https://img.youtube.com/vi/pkKu9hLT-t8/maxresdefault.jpg",
            title = "Trailer #1: Awakening"
        ),
        releaseDate = 1525440713000,
        studio = "Madox",
        author = "Ghost",
        artist = null,
        externalLinks = listOf(
            ExternalLink(site = "Crunchyroll", url = "https://logodix.com/logo/1679460.png"),
            ExternalLink(
                site = "AniList",
                url = "https://anilist.co/",
                iconUrl = "https://anilist.co/img/icons/icon.svg"
            ),
            ExternalLink(
                site = "MyAnimeList",
                url = "https://myanimelist.net/",
                iconUrl = "https://myanimelist.net/images/event/20240226_YSRTM_2024/badge.png?v=17139276"
            )
        ),
        isLoading = false,
        error = null
    )

    val isWideScreen = rememberPlatformConfiguration().isWideScreen

    val fakeEpisodes = listOf(
        Episode("1", 1, "The Beginning", null, null),
        Episode("2", 2, "Cursed Spirit", null, null)
    )
    val tempAnimeList = listOf(
        Anime(
            1,
            ProviderType.MYANIMELIST,
            MediaTitle(romaji = "Shirobako"),
            MediaFormat.TV,
            "https://myanimelist.net/images/manga/3/258224l.webp",
            "",
            "desc",
            listOf("Comedy"),
            MediaReleaseStatus.FINISHED,
            8.0f,
            MediaDate(2014, 10, 9),
            24,
            24,
            "P.A.Works"
        ),
        Anime(
            2,
            ProviderType.ANILIST,
            MediaTitle(romaji = "Kimi no Na wa."),
            MediaFormat.MOVIE,
            "https://myanimelist.net/images/manga/2/287344l.webp",
            "",
            "desc",
            listOf("Romance"),
            MediaReleaseStatus.FINISHED,
            9.0f,
            MediaDate(2016, 8, 26),
            1,
            106,
            "CoMix Wave Films"
        ),
        Anime(
            3,
            ProviderType.MYANIMELIST,
            MediaTitle(romaji = "Mushoku Tensei"),
            MediaFormat.TV,
            "https://myanimelist.net/images/manga/2/253146l.jpg",
            null,
            "desc",
            listOf("Isekai"),
            MediaReleaseStatus.RELEASING,
            7.8f,
            MediaDate(2021, 10, 3),
            23,
            24,
            "Studio Bind"
        ),
        Anime(
            4,
            ProviderType.MYANIMELIST,
            MediaTitle(romaji = "Made-up Adventure"),
            MediaFormat.ONA,
            "https://myanimelist.net/images/manga/1/259070l.webp",
            null,
            "desc",
            listOf("Adventure"),
            MediaReleaseStatus.NOT_YET_RELEASED,
            null,
            MediaDate(2026, 7, 1),
            3,
            12,
            "Indie Studio"
        ),
        Anime(
            5,
            ProviderType.ANILIST,
            MediaTitle(romaji = "Slice of Life Example"),
            MediaFormat.TV_SHORT,
            "https://myanimelist.net/images/manga/3/179882l.webp",
            "banner",
            "desc",
            listOf("Slice of Life"),
            MediaReleaseStatus.FINISHED,
            6.5f,
            MediaDate(2019, 4, 5),
            12,
            8,
            "Shorts Studio"
        )
    )
    val mockReviews = listOf(
        Review(
            id = 1,
            author = "Mika Tanaka",
            authorAvatar = "https://example.com/avatars/mika.jpg",
            score = 90,
            summary = "A beautiful, emotional ride",
            body = "Stunning animation and a soundtrack that stays with you. Character development is top-notch and the pacing kept me hooked until the very end.",
            upvotes = 124,
            isSpoiler = false,
            createdAt = 1682505600000L // 2023-04-26T00:00:00Z
        ),
        Review(
            id = 2,
            author = "DevOnDuty",
            authorAvatar = "https://example.com/avatars/devonduty.png",
            score = 78,
            summary = "Great mechanics, weak finale",
            body = "I loved the worldbuilding and the battle choreography, but the last arc felt rushed and some plot threads were left unresolved.",
            upvotes = 87,
            isSpoiler = true,
            createdAt = 1685097600000L // 2023-05-26T00:00:00Z
        ),
        Review(
            id = 3,
            author = "Aoi",
            authorAvatar = null,
            score = 100,
            summary = "Masterpiece",
            body = "Everything aligned perfectly — story, visuals, and music. One of my all-time favorites; rewatching is a must.",
            upvotes = 340,
            isSpoiler = false,
            createdAt = 1677628800000L // 2023-03-01T00:00:00Z
        ),
        Review(
            id = 4,
            author = "Sam R.",
            authorAvatar = "https://example.com/avatars/samr.jpg",
            score = null,
            summary = null,
            body = "Mixed feelings. The first half is brilliant, but the tone shift later on lost me. Worth trying if you like experimental storytelling.",
            upvotes = 22,
            isSpoiler = false,
            createdAt = 1690848000000L // 2023-07-31T00:00:00Z
        ),
        Review(
            id = 5,
            author = "OtakuLuna",
            authorAvatar = "https://example.com/avatars/luna.jpg",
            score = 85,
            summary = "Solid and fun",
            body = "Funny moments balance the darker themes well. I enjoyed the side characters almost as much as the leads.",
            upvotes = 59,
            isSpoiler = false,
            createdAt = 1693526400000L // 2023-08-31T00:00:00Z
        ),
        Review(
            id = 6,
            author = "CodeSensei",
            authorAvatar = "https://example.com/avatars/codesensei.png",
            score = 70,
            summary = "Good, not great",
            body = "Technical merits are clear — consistent animation and smart direction — but the story repeats familiar beats.",
            upvotes = 15,
            isSpoiler = false,
            createdAt = 1688169600000L // 2023-07-01T00:00:00Z
        ),
        Review(
            id = 7,
            author = "Yuki",
            authorAvatar = "https://example.com/avatars/yuki.jpg",
            score = 92,
            summary = "Heartwarming and profound",
            body = "A quiet show that grows on you. The emotional payoff in the final episodes is incredible without resorting to cheap tricks.",
            upvotes = 201,
            isSpoiler = false,
            createdAt = 1700000000000L // example future timestamp
        ),
        Review(
            id = 8,
            author = "NoSpoilerPlease",
            authorAvatar = null,
            score = 60,
            summary = "Not for everyone",
            body = "If you prefer straightforward plots, this might frustrate you. It experiments a lot, which is admirable, but it won't click with every viewer.",
            upvotes = 8,
            isSpoiler = false,
            createdAt = 1675209600000L // 2023-02-01T00:00:00Z
        )
    )

    MaterialTheme {
        MediaDetailContent(
            state = state,
            episodes = fakeEpisodes.toPagingItems(),
            recommendations = tempAnimeList.toPagingItems(),
            reviews = mockReviews.toPagingItems(),
            onEvent = {},
            config = MediaDetailUiConfig(),
            isWideScreen = isWideScreen
        )
    }
}