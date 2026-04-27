package com.ghost.zeku.presentation.screen.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage
import com.ghost.zeku.domain.model.common.MediaDate
import com.ghost.zeku.domain.model.common.MediaTitle
import com.ghost.zeku.domain.model.enum.*
import com.ghost.zeku.domain.model.media.*
import com.ghost.zeku.presentation.common.MediaAsyncImage
import com.ghost.zeku.presentation.common.chips.GenreChip
import com.ghost.zeku.presentation.common.isDesktop
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.character.MediaCharacterCard
import com.ghost.zeku.presentation.components.media.character.MediaCharacterCardConfig
import com.ghost.zeku.presentation.components.media.episode.EpisodeCard
import com.ghost.zeku.presentation.components.media.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.media.poster.PosterConfig
import com.ghost.zeku.presentation.components.media.poster.toPosterUiData
import com.ghost.zeku.presentation.components.media.relation.MediaRelationCard
import com.ghost.zeku.presentation.components.media.relation.MediaRelationCardConfig
import com.ghost.zeku.presentation.components.media.review.ReviewCard
import com.ghost.zeku.presentation.components.media.review.ReviewCardConfig
import com.ghost.zeku.presentation.components.section.*
import com.ghost.zeku.utils.formatTimestamp
import com.ghost.zeku.utils.toPagingItems
import org.jetbrains.compose.resources.stringResource
import zeku.composeapp.generated.resources.*


@Immutable
data class MediaDetailUiConfig(
    // Section configs
    val characterSection: MediaSectionConfig = MediaSectionConfig(),
    val relationSection: MediaSectionConfig = MediaSectionConfig(),
    val reviewSection: MediaSectionConfig = MediaSectionConfig(
        layout = SectionLayout.VerticalList()
    ),
    val recommendationSection: MediaSectionConfig = MediaSectionConfig(),

    // Card configs
    val characterCard: MediaCharacterCardConfig = MediaCharacterCardConfig(),
    val relationCard: MediaRelationCardConfig = MediaRelationCardConfig(),
    val reviewCard: ReviewCardConfig = ReviewCardConfig(),
    val recommendationCard: PosterConfig = PosterConfig(),


    // Feature flags (only when needed)
    val showTrailer: Boolean = true,
    val showExternalLinks: Boolean = true
)


@Composable
fun MediaDetailContent(
    state: MediaDetailContract.State,
    episodes: LazyPagingItems<Episode>,
    recommendations: LazyPagingItems<Anime>,
    reviews: LazyPagingItems<Review>,
    onEvent: (MediaDetailContract.Event) -> Unit,
    config: MediaDetailUiConfig,
    isDesktop: Boolean = false
) {
    val layoutDirection = LocalLayoutDirection.current
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
                if (isDesktop) {
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
                    .padding(16.dp)
                    .scale(1f) // enable press animation
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Outlined.ChatBubble, contentDescription = null)
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
    // The ROOT is a LazyColumn so the whole page, including the Hero, scrolls together.
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. HERO SECTION (Full Width, at the top)
        item {
            Box(
                modifier = Modifier.layout { measurable, constraints ->
                    // Measure the HeroSection normally (e.g., 530.dp)
                    val placeable = measurable.measure(constraints)

                    // The exact amount you want the bottom content to overlap the Hero
                    val overlap = 140.dp.roundToPx()

                    // Report a smaller height to the LazyColumn so it pulls the next items up!
                    layout(placeable.width, placeable.height - overlap) {
                        placeable.placeRelative(0, 0)
                    }
                }
            ) {
                HeroSection(state, isDesktop = true)
            }
        }

        // 2. THE SPLIT ROW (Contains everything EXCEPT the paged reviews)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // LEFT PANEL (Static Content)
                // We use a standard Column here because it's already inside a scrolling parent
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    SynopsisSection(state.description)
                    AnimatedVisibility(config.showTrailer) {
                        TrailerSection(state.trailer, onEvent)
                    }

                    RecommendationSection(
                        modifier = Modifier,
                        recommendations = recommendations,
                        sectionConfig = config.recommendationSection,
                        cardConfig = config.recommendationCard,
                        onAction = { /* TODO */ })
                    CharacterSection(
                        modifier = Modifier, characters = state.characters, onAction = { /* TODO */ },
                        sectionConfig = config.characterSection,
                        cardConfig = config.characterCard
                    )
                }

                // RIGHT PANEL (Sidebar)
                Column(
                    modifier = Modifier.width(320.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    InfoPanel(state)
                    AnimatedVisibility(config.showExternalLinks) {
                        ExternalLinksPanel(state.externalLinks, isMobile = false)
                    }
                    RelationSection(
                        modifier = Modifier,
                        relations = state.relations,
                        sectionConfig = config.relationSection,
                        cardConfig = config.relationCard,
                        onAction = { /* TODO */ })
                }
            }
        }

        // 3. THE PAGED REVIEWS
        // Because reviewSection operates on the LazyListScope, it goes directly in the root here.
        reviewSection(
            reviews = reviews,
            isDesktop = true,
            sectionConfig = config.reviewSection,
            cardConfig = config.reviewCard
        )
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. THE OVERLAP TRICK
        item {
            Box(
                modifier = Modifier.layout { measurable, constraints ->
                    // Measure the HeroSection normally (e.g., 530.dp)
                    val placeable = measurable.measure(constraints)

                    // The exact amount you want the bottom content to overlap the Hero
                    val overlap = 100.dp.roundToPx()

                    // Report a smaller height to the LazyColumn so it pulls the next items up!
                    layout(placeable.width, placeable.height - overlap) {
                        placeable.placeRelative(0, 0)
                    }
                }
            ) {
                HeroSection(state, isDesktop = false)
            }
        }

        // Because of the layout modifier above, this Spacer and Button
        // will now draw DIRECTLY on top of the bottom 100dp of the HeroSection!
//        item { Spacer(Modifier.height(16.dp)) }

        item {
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.watch_now), style = MaterialTheme.typography.titleMedium)
            }
        }
        item { SynopsisSection(state.description) }
        if (config.showTrailer) {
            item { TrailerSection(state.trailer, onEvent) }
        }



        item {
            CharacterSection(
                modifier = Modifier,
                characters = state.characters,
                onAction = { /* TODO */ },
                sectionConfig = config.characterSection,
                cardConfig = config.characterCard
            )
        }

        if (config.showExternalLinks) {
            item { ExternalLinksPanel(state.externalLinks, isMobile = true) }
        }

        item {
            RelationSection(
                modifier = Modifier,
                relations = state.relations,
                sectionConfig = config.relationSection,
                cardConfig = config.relationCard,
                onAction = { /* TODO */ }
            )
        }

        item {
            RecommendationSection(
                modifier = Modifier,
                recommendations = recommendations,
                sectionConfig = config.recommendationSection,
                cardConfig = config.recommendationCard,
                onAction = { /* TODO */ })
        }

        reviewSection(
            reviews = reviews,
            isDesktop = false,
            sectionConfig = config.reviewSection,
            cardConfig = config.reviewCard
        )
    }
}


@Composable
private fun HeroSection(state: MediaDetailContract.State, isDesktop: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isDesktop) 716.dp else 590.dp)
    ) {
        val imageUrl = state.bannerImage ?: state.coverImage

        // 1. Base Background Image (Sharp)
        MediaAsyncImage(
            url = imageUrl,
            contentDescription = state.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. The Gradient Blur Mask
        // This layers a blurred version of the image on top, but fades it in at the bottom.
        if (isDesktop) {
            MediaAsyncImage(
                url = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(24.dp)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    // Use drawWithCache instead of drawWithContent to prevent recreating
                    // the Brushes on every single frame, which saves memory!
                    .drawWithCache {
                        // 1. The Left-to-Right Blur Mask
                        // Note: For DstIn masks, the color doesn't matter, ONLY the alpha matters.
                        // Black = 100% Blur visible. Transparent = 0% Blur visible.
                        val horizontalMask = Brush.horizontalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startX = 0f,
                            endX = size.width * 0.9f // Fades out at 60% of the screen width
                        )

                        // 2. The Top-to-Bottom Blur Mask
                        val verticalMask = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = size.height * 0.4f,
                            endY = size.height
                        )

                        onDrawWithContent {
                            // Draw the blurred image onto the canvas
                            drawContent()

                            // Open a raw Canvas layer to combine our gradients
                            drawIntoCanvas { canvas ->
                                // saveLayer tells Compose: "Group everything I draw next into a single
                                // flat layer, and when I'm done, apply it to the image using DstIn."
                                canvas.saveLayer(
                                    bounds = Rect(0f, 0f, size.width, size.height),
                                    paint = Paint().apply { blendMode = BlendMode.DstIn }
                                )

                                // Draw the horizontal mask
                                drawRect(horizontalMask)

                                // Draw the vertical mask ON TOP of the horizontal one.
                                // Compose naturally merges them together into a perfect "L" shape.
                                drawRect(verticalMask)

                                // Apply the finished mask to the blurred image!
                                canvas.restore()
                            }
                        }
                    },
                contentScale = ContentScale.Crop
            )
        } else {
            MediaAsyncImage(
                url = imageUrl,
                contentDescription = null, // decorative
                modifier = Modifier
                    .fillMaxSize()
                    .blur(24.dp) // The intensity of the blur
                    .graphicsLayer {
                        // Offscreen compositing is required for BlendMode masking to work
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        // Draw a gradient mask that reveals the blur at the bottom
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = size.height * 0.4f, // Start blurring 40% down the image
                                endY = size.height
                            ),
                            blendMode = BlendMode.DstIn // Keeps the image only where the gradient is Black
                        )
                    },
                contentScale = ContentScale.Crop
            )
        }

        // 3. The Color Scrim
        // Fades into your actual MaterialTheme background color
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f // Let Compose automatically calculate the endY based on the Box height
                )
            )
        )

        if (isDesktop) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                            Color.Transparent,
                        ),
                        startX = 0f,
                    )
                )
            )
        }


        // 4. Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = if (isDesktop) 140.dp else 100.dp)
        ) {
            if (isDesktop) {
                Row(verticalAlignment = Alignment.Bottom) {
                    // Poster on desktop
                    MediaAsyncImage(
                        url = state.coverImage,
                        contentDescription = stringResource(Res.string.poster),
                        modifier = Modifier
                            .width(200.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        MetadataAndActions(state, isDesktop = true)
                    }
                }
            } else {
                MetadataAndActions(state, isDesktop = false)
            }
        }
    }
}


@Composable
private fun MetadataAndActionsV2(state: MediaDetailContract.State, isDesktop: Boolean) {
    // 1. Define a smooth, dark shadow
    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.8f),
        offset = Offset(0f, 4f), // Drop it down slightly
        blurRadius = 12f // Blur it out so it looks like a natural glow, not a hard duplicate
    )

    // 2. Force light text colors since it's sitting on an image
    // (The gradient scrim we added earlier is usually dark)
    val primaryTextColor = Color.White
    val secondaryTextColor = Color.White.copy(alpha = 0.7f)

    // Genres (These are already in Surface chips, so they are highly visible)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.genres.forEach { genre ->
            val isFirst = genre == state.genres.first()
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isFirst) MaterialTheme.colorScheme.secondaryContainer
                else Color.White.copy(alpha = 0.2f), // Frosted look for secondary genres
                contentColor = if (isFirst) MaterialTheme.colorScheme.onSecondaryContainer
                else Color.White
            ) {
                Text(
                    text = genre,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // Title with Shadow
    Text(
        text = state.title,
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            shadow = textShadow // Apply shadow here
        ),
        color = primaryTextColor
    )

    Spacer(Modifier.height(8.dp))

    // Metadata Row
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = " ${state.rating}",
            style = MaterialTheme.typography.titleSmall.copy(shadow = textShadow),
            color = primaryTextColor
        )

        Text(
            stringResource(Res.string.dot_separator),
            color = secondaryTextColor,
            style = TextStyle(shadow = textShadow)
        )

        Text(
            text = state.releaseDate.toString() ?: "Unknown",
            style = MaterialTheme.typography.labelMedium.copy(shadow = textShadow),
            color = secondaryTextColor
        )

        Text(
            stringResource(Res.string.dot_separator),
            color = secondaryTextColor,
            style = TextStyle(shadow = textShadow)
        )

        Text(
            text = state.studio ?: state.author ?: "Unknown",
            style = MaterialTheme.typography.labelMedium.copy(shadow = textShadow),
            color = secondaryTextColor
        )
    }

    if (isDesktop) {
        Spacer(Modifier.height(16.dp))
        // Desktop Actions
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                Text(stringResource(Res.string.watch_now), color = MaterialTheme.colorScheme.onSecondary)
            }

            // Replaced OutlinedButton with a transparent Surface for better visibility over images
            Surface(
                onClick = {},
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                    Text(
                        stringResource(Res.string.watchlist),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}


@Composable
private fun MetadataAndActions(state: MediaDetailContract.State, isDesktop: Boolean) {
    // Genres
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.genres.forEach { genre ->
            GenreChip(genre)
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(state.title, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold))
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            " ${state.rating}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(stringResource(Res.string.dot_separator), color = MaterialTheme.colorScheme.outline)
        Text(
            text = formatTimestamp(state.releaseDate ?: 0),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(stringResource(Res.string.dot_separator), color = MaterialTheme.colorScheme.outline)
        Text(
            state.studio ?: state.author ?: stringResource(Res.string.unknown),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (isDesktop) {
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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


// ---------- Synopsis ----------
@Composable
private fun SynopsisSection(synopsis: String?) {
    SectionHeader(title = stringResource(Res.string.synopsis))
    Text(
        synopsis ?: stringResource(Res.string.no_description),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// ---------- Trailer ----------
@Composable
private fun TrailerSection(trailer: MediaTrailer?, onEvent: (MediaDetailContract.Event) -> Unit) {
    SectionHeader(title = stringResource(Res.string.official_trailer))
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { TODO() }
    ) {
        MediaAsyncImage(
            url = trailer?.thumbnail ?: "",
            contentDescription = stringResource(Res.string.official_trailer),
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = stringResource(Res.string.play),
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.align(Alignment.Center).size(64.dp)
        )
        Text(
            "Trailer #1: Awakening", modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            style = MaterialTheme.typography.titleMedium, color = Color.White
        )
    }
}


// ---------- Section Header ----------
@Composable
private fun SectionHeader(title: String, action: String? = null) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (action != null) {
            TextButton(onClick = {}) {
                Text(action, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CharacterSection(
    modifier: Modifier = Modifier,
    characters: List<MediaCharacter>,
    sectionConfig: MediaSectionConfig,
    cardConfig: MediaCharacterCardConfig,
    onAction: (MediaAction) -> Unit
) {
    MediaSection(
        title = stringResource(Res.string.characters),
        items = characters,
        onViewAllClick = { /* TODO */ },
        config = sectionConfig,
        modifier = modifier,
        key = null,
        isLoading = false,
    ) { character, modifier ->
        MediaCharacterCard(
            character = character,
            config = cardConfig,
            modifier = modifier,
            onAction = onAction
        )
    }
}


@Composable
fun RelationSection(
    modifier: Modifier = Modifier,
    relations: List<MediaRelation>,
    sectionConfig: MediaSectionConfig,
    cardConfig: MediaRelationCardConfig,
    onAction: (MediaAction) -> Unit
) {
    MediaSection(
        title = stringResource(Res.string.relations),
        items = relations,
        onViewAllClick = { /* TODO */ },
        config = sectionConfig,
        key = null,
        isLoading = false,
        modifier = modifier
    ) { relation, modifier ->
        MediaRelationCard(
            relation = relation,
            config = cardConfig,
            modifier = modifier,
            onAction = onAction
        )
    }
}


@Composable
fun EpisodeSection(
    modifier: Modifier = Modifier,
    episodes: LazyPagingItems<Episode>,
    onEvent: (MediaAction) -> Unit
) {
    PagedMediaSection(
        modifier = modifier,
        title = stringResource(Res.string.episodes),
        items = episodes,
        config = MediaSectionConfig(
            layout = SectionLayout.VerticalList()
        ),
    ) { episode, modifier ->
        EpisodeCard(
            episode = episode,
            modifier = modifier,
            onAction = onEvent
        )
    }
}


@Composable
fun RecommendationSection(
    modifier: Modifier = Modifier,
    recommendations: LazyPagingItems<Anime>,
    sectionConfig: MediaSectionConfig,
    cardConfig: PosterConfig,
    onAction: (MediaAction) -> Unit
) {
    PagedMediaSection(
        modifier = modifier,
        title = stringResource(Res.string.more_like_this),
        config = sectionConfig,
        items = recommendations,
    ) { media, modifier ->
        MediaPosterCard(
            data = media.toPosterUiData(),
            modifier = modifier,
            onAction = onAction,
            config = cardConfig
        )
    }
}


fun LazyListScope.reviewSection(
    reviews: LazyPagingItems<Review>,
    sectionConfig: MediaSectionConfig,
    cardConfig: ReviewCardConfig,
    isDesktop: Boolean,
) {
    // Inject the vertical paged items directly into the root layout
    pagedMediaSection(
        title = "Reviews",
        items = reviews,
        config = sectionConfig
    ) { review, modifier ->
        ReviewCard(
            review = review,
            config = cardConfig,
            modifier = modifier
        )
    }
}


// ---------- External Links ----------
@Composable
private fun ExternalLinksPanel(links: List<ExternalLink>, isMobile: Boolean = false) {
    SectionHeader(title = stringResource(Res.string.watch_follow))
    val arranged = if (isMobile) { // mobile layout: horizontal scroll
        LazyRow(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(links) { link -> ExternalLinkItem(link) }
        }
    } else {
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            links.forEach { link -> ExternalLinkItem(link) }
        }
    }
}

@Composable
private fun ExternalLinkItem(link: ExternalLink) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = link.iconUrl,
                contentDescription = link.site,
            )
            Spacer(Modifier.width(8.dp))
            Text(link.site, style = MaterialTheme.typography.labelSmall)
        }
    }
}


// ---------- Info Panel (Desktop) ----------
@Composable
private fun InfoPanel(state: MediaDetailContract.State) {
    GlassCard {
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(Res.string.information),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.height(12.dp))
            InfoRow("Status", "Currently Airing")
            InfoRow("Format", "TV Series")
            InfoRow("Premiered", "Fall 2024")
            InfoRow("Duration", "24 mins / ep")
            InfoRow("Rating", "PG-13 (Teens)")
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Text(
                "Producers",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        "Arkhos Media",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        "Global Anime Dist.",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


// ---------- Glass Card Component ----------
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        onClick = onClick ?: {}
    ) {
        content()
    }
}


@Preview(showBackground = true, widthDp = 400)
@Composable
fun PreviewMediaDetailContent() {

    // -------------------------
    // FAKE STATE
    // -------------------------
    val state = MediaDetailContract.State(
        id = 1,
        title = "Jujutsu Kaisen",
        description = "A boy swallows a cursed finger and becomes part of a dark world of sorcery.",
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
                id = 10,
                relationType = RelationType.SEQUEL,
                title = MediaTitle(romaji = "Jujutsu Kaisen 0", null, null),
                coverImage = "https://myanimelist.net/images/manga/1/157897l.webp",
                mediaType = MediaType.ANIME,
                format = MediaFormat.MOVIE
            ),
            MediaRelation(
                id = 11,
                relationType = RelationType.PREQUEL,
                title = MediaTitle(romaji = "Jujutsu Kaisen: Final", null, null),
                coverImage = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx101922-WBsBl0ClmgYL.jpg",
                mediaType = MediaType.ANIME,
                format = MediaFormat.MOVIE
            )
        ),
        trailer = MediaTrailer(
            id = "xdsss",
            site = "Crunchy Role",
            thumbnail = "https://img.youtube.com/vi/pkKu9hLT-t8/maxresdefault.jpg",
        ),
        releaseDate = 1525440713000,
        studio = "Madox",
        author = "Ghost",
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


    val isDesktop = rememberPlatformConfiguration().isDesktop

    // -------------------------
    // FAKE PAGING
    // -------------------------

    val fakeEpisodes = listOf(
        Episode("1", 1, "The Beginning", null, null),
        Episode("2", 2, "Cursed Spirit", null, null)
    )

    val tempAnimeList = listOf(
        Anime(
            id = 1,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(romaji = "Shirobako", english = "Shirobako", native = "SHIROBAKO"),
            format = MediaFormat.TV,
            coverImage = "https://myanimelist.net/images/manga/3/258224l.webp",
            bannerImage = "https://example.com/banners/shirobako.jpg",
            description = "A group of young women working in the anime industry struggle to complete their first major production.",
            genres = listOf("Comedy", "Drama", "Slice of Life"),
            status = MediaReleaseStatus.FINISHED,
            score = 8.0f,
            startDate = MediaDate(year = 2014, month = 10, day = 9),
            episodes = 24,
            duration = 24,
            studio = "P.A.Works",
        ),
        Anime(
            id = 2,
            source = ProviderType.ANILIST,
            title = MediaTitle(romaji = "Kimi no Na wa.", english = "Your Name.", native = "君の名は。"),
            format = MediaFormat.MOVIE,
            coverImage = "https://myanimelist.net/images/manga/2/287344l.webp",
            bannerImage = "https://example.com/banners/your_name.jpg",
            description = "Two teenagers share a profound, magical connection when they begin to swap bodies.",
            genres = listOf("Romance", "Supernatural", "Drama"),
            status = MediaReleaseStatus.FINISHED,
            score = 9.0f,
            startDate = MediaDate(year = 2016, month = 8, day = 26),
            episodes = 1,
            duration = 106,
            studio = "CoMix Wave Films",
        ),
        Anime(
            id = 3,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(
                romaji = "Mushoku Tensei: Isekai Ittara Honki Dasu",
                english = "Mushoku Tensei",
                native = "無職転生"
            ),
            format = MediaFormat.TV,
            coverImage = "https://myanimelist.net/images/manga/2/253146l.jpg",
            bannerImage = null,
            description = "A jobless and hopeless man is reincarnated in a fantasy world as Rudeus Greyrat and resolves to live his new life to the fullest.",
            genres = listOf("Isekai", "Fantasy", "Drama"),
            status = MediaReleaseStatus.RELEASING,
            score = 7.8f,
            startDate = MediaDate(year = 2021, month = 10, day = 3),
            episodes = 23,
            duration = 24,
            studio = "Studio Bind",
        ),
        Anime(
            id = 4,
            source = ProviderType.MYANIMELIST,
            title = MediaTitle(romaji = "Made-up Adventure", english = "Made-up Adventure", native = null),
            format = MediaFormat.ONA,
            coverImage = "https://myanimelist.net/images/manga/1/259070l.webp",
            bannerImage = null,
            description = "A short original net animation about an inventor and their robot companion.",
            genres = listOf("Adventure", "Sci-Fi"),
            status = MediaReleaseStatus.NOT_YET_RELEASED,
            score = null,
            startDate = MediaDate(year = 2026, month = 7, day = 1),
            episodes = 3,
            duration = 12,
            studio = "Indie Studio",
            trackEntry = null
        ),
        Anime(
            id = 5,
            source = ProviderType.ANILIST,
            title = MediaTitle(
                romaji = "Slice of Life Example",
                english = "Slice of Life Example",
                native = "日常の例"
            ),
            format = MediaFormat.TV_SHORT,
            coverImage = "https://myanimelist.net/images/manga/3/179882l.webp",
            bannerImage = "https://example.com/banners/slice_of_life.jpg",
            description = "Everyday moments from a quirky group's daily life.",
            genres = listOf("Slice of Life", "Comedy"),
            status = MediaReleaseStatus.FINISHED,
            score = 6.5f,
            startDate = MediaDate(year = 2019, month = 4, day = 5),
            episodes = 12,
            duration = 8,
            studio = "Shorts Studio",
        )
    )


    val fakeReviews = listOf(
        Review(
            id = 1,
            author = "User1",
            authorAvatar = null,
            score = 90,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),
        Review(
            id = 2,
            author = "User2",
            authorAvatar = null,
            score = 90,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),
        Review(
            id = 3,
            author = "User3",
            authorAvatar = null,
            score = 23,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),
        Review(
            id = 11,
            author = "User1",
            authorAvatar = null,
            score = 90,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),
        Review(
            id = 22,
            author = "User2",
            authorAvatar = null,
            score = 90,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),
        Review(
            id = 33,
            author = "User3",
            authorAvatar = null,
            score = 23,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),
        Review(
            id = 31,
            author = "User1",
            authorAvatar = null,
            score = 90,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),
        Review(
            id = 12,
            author = "User2",
            authorAvatar = null,
            score = 90,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),
        Review(
            id = 43,
            author = "User3",
            authorAvatar = null,
            score = 23,
            summary = "Amazing anime!",
            body = "Loved everything about it."
        ),

        )

    // Convert to LazyPagingItems using helper
    val episodesPaging = fakeEpisodes.toPagingItems()
    val animePaging = tempAnimeList.toPagingItems()
    val reviewsPaging = fakeReviews.toPagingItems()
    val related = tempAnimeList.toPagingItems()

    MaterialTheme {
        MediaDetailContent(
            state = state,
            episodes = episodesPaging,
            recommendations = animePaging,
            reviews = reviewsPaging,
            onEvent = {},
            config = MediaDetailUiConfig(),
            isDesktop = isDesktop
        )
    }
}


