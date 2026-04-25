//package com.ghost.zeku.presentation.screen.detail
//
//import androidx.compose.foundation.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.ghost.zeku.domain.model.media.MediaCharacter
//import com.ghost.zeku.presentation.common.MediaAsyncImage
//import com.ghost.zeku.presentation.common.isDesktop
//import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
//import org.jetbrains.compose.resources.stringResource
//import zeku.composeapp.generated.resources.*
//
//
//// ---------- Main Screen ----------
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun MediaDetailScreen(viewModel: MediaDetailViewModel = viewModel()) {
//    val state by viewModel.state.collectAsState()
//    val configuration = rememberPlatformConfiguration()
//    val isDesktop = configuration.isDesktop
//
//
//    MediaDetailContent(
//        state = state,
//        isDesktop = isDesktop,
//        onIntent = viewModel::processIntent
//    )
//}
//
//@Composable
//fun MediaDetailContent(
//    state: MediaDetailState,
//    isDesktop: Boolean,
//    onIntent: (MediaDetailIntent) -> Unit
//) {
//    Scaffold(
//        containerColor = MaterialTheme.colorScheme.background,
//    ) { padding ->
//        Box(Modifier.padding(padding)) {
//            if (state.isLoading) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            } else {
//                if (isDesktop) {
//                    DesktopLayout(state, onIntent)
//                } else {
//                    MobileLayout(state, onIntent)
//                }
//            }
//
//            // Floating chat button
//            FloatingActionButton(
//                onClick = { /* chat */ },
//                containerColor = MaterialTheme.colorScheme.secondary,
//                contentColor = MaterialTheme.colorScheme.onSecondary,
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(16.dp)
//                    .scale(1f) // enable press animation
//                    .clip(RoundedCornerShape(16.dp))
//            ) {
//                Icon(Icons.Outlined.ChatBubble, contentDescription = "Chat")
//            }
//        }
//    }
//}
//
//
//// ---------- Bottom Navigation (Mobile) ----------
//@Composable
//private fun BottomNavBar() {
//    NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) {
//        NavigationBarItem(
//            selected = true,
//            onClick = {},
//            icon = { Icon(Icons.Filled.Home, "Home") },
//            label = { Text("Home") })
//        NavigationBarItem(
//            selected = false,
//            onClick = {},
//            icon = { Icon(Icons.Outlined.Explore, "Browse") },
//            label = { Text("Browse") })
//        NavigationBarItem(
//            selected = false,
//            onClick = {},
//            icon = { Icon(Icons.Outlined.Subscriptions, "Library") },
//            label = { Text("Library") })
//        NavigationBarItem(
//            selected = false,
//            onClick = {},
//            icon = { Icon(Icons.Outlined.Person, "Profile") },
//            label = { Text("Profile") })
//    }
//}
//
//// ---------- Desktop Layout ----------
//@Composable
//private fun DesktopLayout(state: MediaDetailState, onIntent: (MediaDetailIntent) -> Unit) {
//    Row(Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            HeroSection(state, isDesktop = true)
//            SynopsisSection(state.synopsis)
//            TrailerSection(state.trailer, onIntent)
//            CharactersSection(state.characters)
//            EpisodesSection(state.episodes, onIntent)
//            ReviewsSection(state.reviews, onIntent)
//        }
//        Column(
//            modifier = Modifier.width(320.dp).verticalScroll(rememberScrollState())
//                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp),
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            InfoPanel(state)
//            ExternalLinksPanel(state.externalLinks)
//            RelationsSection(state.relations)
//            RecommendationsSection(state.recommendations)
//        }
//    }
//}
//
//// ---------- Mobile Layout ----------
//@Composable
//private fun MobileLayout(state: MediaDetailState, onIntent: (MediaDetailIntent) -> Unit) {
//    Column(Modifier.verticalScroll(rememberScrollState())) {
//        HeroSection(state, isDesktop = false)
//        Spacer(Modifier.height(16.dp))
//        // Watch Now button
//        Button(
//            onClick = { onIntent(MediaDetailIntent.WatchNowClicked) },
//            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Icon(
//                Icons.Filled.PlayArrow,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.onTertiaryContainer
//            )
//            Spacer(Modifier.width(8.dp))
//            Text(stringResource(Res.string.watch_now), style = MaterialTheme.typography.titleMedium)
//        }
//        TrailerSection(state.trailerThumbnailUrl, onIntent)
//        SynopsisSection(state.synopsis)
//        CharactersSection(state.characters)
//        RelationsSection(state.relations)
//        ExternalLinksPanel(state.externalLinks, isMobile = true)
//        ReviewsSection(state.reviews, onIntent)
//        EpisodesSection(state.episodes, onIntent)
//        RecommendationsSection(state.recommendations)
//        Spacer(Modifier.height(80.dp)) // space for bottom nav
//    }
//}
//
//// ---------- Hero Section ----------
//@Composable
//private fun HeroSection(state: MediaDetailState, isDesktop: Boolean) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(if (isDesktop) 716.dp else 530.dp)
//    ) {
//        // Background image
//        MediaAsyncImage(
//            url = state.heroImageUrl,
//            contentDescription = state.title,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//        // Gradient scrim
//        Box(
//            modifier = Modifier.fillMaxSize().background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        Color.Transparent,
//                        MaterialTheme.colorScheme.background
//                    ),
//                    startY = 0f,
//                    endY = Float.POSITIVE_INFINITY
//                )
//            )
//        )
//        // Content
//        Column(
//            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).padding(bottom = 24.dp)
//        ) {
//            if (isDesktop) {
//                Row(verticalAlignment = Alignment.Bottom) {
//                    // Poster on desktop
//                    MediaAsyncImage(
//                        url = state.posterUrl,
//                        contentDescription = "Poster",
//                        modifier = Modifier
//                            .width(200.dp)
//                            .aspectRatio(2f / 3f)
//                            .clip(RoundedCornerShape(12.dp))
//                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
//                    )
//                    Spacer(Modifier.width(24.dp))
//                    Column(modifier = Modifier.weight(1f)) {
//                        MetadataAndActions(state, isDesktop = true)
//                    }
//                }
//            } else {
//                MetadataAndActions(state, isDesktop = false)
//            }
//        }
//    }
//}
//
//@Composable
//private fun MetadataAndActions(state: MediaDetailState, isDesktop: Boolean) {
//    // Genres
//    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//        state.genres.forEach { genre ->
//            Surface(
//                shape = RoundedCornerShape(50),
//                color = if (genre == state.genres.first()) MaterialTheme.colorScheme.secondaryContainer
//                else MaterialTheme.colorScheme.surfaceVariant,
//                contentColor = if (genre == state.genres.first()) MaterialTheme.colorScheme.onSecondaryContainer
//                else MaterialTheme.colorScheme.onSurfaceVariant
//            ) {
//                Text(
//                    genre, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
//                    style = MaterialTheme.typography.labelMedium
//                )
//            }
//        }
//    }
//    Spacer(Modifier.height(8.dp))
//    Text(state.title, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold))
//    Spacer(Modifier.height(8.dp))
//    Row(verticalAlignment = Alignment.CenterVertically) {
//        Icon(
//            Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary,
//            modifier = Modifier.size(18.dp)
//        )
//        Text(
//            " ${state.rating}",
//            style = MaterialTheme.typography.titleSmall,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//        Text(" • ", color = MaterialTheme.colorScheme.outline)
//        Text(
//            "${state.year}",
//            style = MaterialTheme.typography.labelMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Text(" • ", color = MaterialTheme.colorScheme.outline)
//        Text(
//            "${state.episodeCount} Episodes",
//            style = MaterialTheme.typography.labelMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Text(" • ", color = MaterialTheme.colorScheme.outline)
//        Text(
//            state.studio,
//            style = MaterialTheme.typography.labelMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//    if (isDesktop) {
//        Spacer(Modifier.height(16.dp))
//        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//            Button(
//                onClick = {},
//                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
//                shape = RoundedCornerShape(50)
//            ) {
//                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
//                Text(stringResource(Res.string.watch_now), color = MaterialTheme.colorScheme.onSecondary)
//            }
//            OutlinedButton(
//                onClick = {},
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
//            ) {
//                Icon(Icons.Outlined.Add, contentDescription = null)
//                Text(stringResource(Res.string.watchlist))
//            }
//        }
//    }
//}
//
//// ---------- Synopsis ----------
//@Composable
//private fun SynopsisSection(synopsis: String) {
//    SectionHeader(title = stringResource(Res.string.synopsis))
//    Text(
//        synopsis, style = MaterialTheme.typography.bodyLarge,
//        color = MaterialTheme.colorScheme.onSurfaceVariant,
//        modifier = Modifier.padding(horizontal = 16.dp)
//    )
//}
//
//// ---------- Trailer ----------
//@Composable
//private fun TrailerSection(thumbnailUrl: String, onIntent: (MediaDetailIntent) -> Unit) {
//    SectionHeader(title = stringResource(Res.string.official_trailer))
//    Box(
//        modifier = Modifier
//            .padding(horizontal = 16.dp)
//            .fillMaxWidth()
//            .aspectRatio(16f / 9f)
//            .clip(RoundedCornerShape(16.dp))
//            .clickable { onIntent(MediaDetailIntent.TrailerPlayClicked) }
//    ) {
//        MediaAsyncImage(url = thumbnailUrl, contentDescription = "Trailer", modifier = Modifier.fillMaxSize())
//        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
//        Icon(
//            Icons.Filled.PlayArrow,
//            contentDescription = "Play",
//            tint = MaterialTheme.colorScheme.onSecondary,
//            modifier = Modifier.align(Alignment.Center).size(64.dp)
//        )
//        Text(
//            "Trailer #1: Awakening", modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
//            style = MaterialTheme.typography.titleMedium, color = Color.White
//        )
//    }
//}
//
//// ---------- Characters ----------
//@Composable
//private fun CharactersSection(characters: List<MediaCharacter>) {
//    SectionHeader(title = stringResource(Res.string.characters_cast), action = "View All")
//    if (characters.isEmpty()) return
//    Row(
//        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
//        horizontalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        characters.forEach { char ->
//            GlassCard(modifier = Modifier.width(140.dp).height(180.dp)) {
//                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//                    MediaAsyncImage(
//                        url = char.imageUrl,
//                        contentDescription = char.name,
//                        modifier = Modifier.size(64.dp).clip(CircleShape),
//                        contentScale = ContentScale.Crop
//                    )
//                    Spacer(Modifier.height(8.dp))
//                    Text(char.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
//                    Text(
//                        char.role,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        "JP: ${char.japaneseVA}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        "EN: ${char.englishVA}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ---------- Episodes (Desktop version uses full expanded card) ----------
//@Composable
//private fun EpisodesSection(episodes: List<Episode>, onIntent: (MediaDetailIntent) -> Unit) {
//    SectionHeader(title = stringResource(Res.string.episodes), action = "Sort / Search")
//    episodes.forEach { ep ->
//        EpisodeCard(episode = ep, onIntent = onIntent)
//    }
//}
//
//@Composable
//private fun EpisodeCard(episode: Episode, onIntent: (MediaDetailIntent) -> Unit) {
//    GlassCard(
//        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
//        onClick = { /* play episode */ }
//    ) {
//        Row(Modifier.padding(12.dp)) {
//            MediaAsyncImage(
//                url = episode.thumbnailUrl,
//                contentDescription = "Episode ${episode.number}",
//                modifier = Modifier.width(100.dp).height(70.dp).clip(RoundedCornerShape(8.dp)),
//                contentScale = ContentScale.Crop
//            )
//            Spacer(Modifier.width(12.dp))
//            Column(Modifier.weight(1f)) {
//                Row {
//                    Text(
//                        "${episode.number}. ${episode.title}",
//                        style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(Modifier.weight(1f))
//                    Text(
//                        "EP ${episode.number}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//                Text(
//                    episode.description,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    style = MaterialTheme.typography.bodySmall
//                )
//                Spacer(Modifier.height(4.dp))
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp))
//                    Text(episode.airDate, style = MaterialTheme.typography.labelSmall)
//                    Spacer(Modifier.width(12.dp))
//                    if (episode.watched) {
//                        Icon(
//                            Icons.Filled.Visibility,
//                            contentDescription = "Watched",
//                            tint = MaterialTheme.colorScheme.secondary,
//                            modifier = Modifier.size(12.dp)
//                        )
//                        Text(
//                            "Watched",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.secondary
//                        )
//                    } else if (episode.progressPercent != null) {
//                        LinearProgressIndicator(
//                            progress = episode.progressPercent,
//                            modifier = Modifier.weight(1f).height(4.dp),
//                            color = MaterialTheme.colorScheme.secondary,
//                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
//                        )
//                        Text("${(episode.progressPercent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
//                    }
//                }
//            }
//            Text(
//                episode.duration,
//                style = MaterialTheme.typography.labelSmall,
//                modifier = Modifier.padding(start = 4.dp)
//            )
//        }
//    }
//}
//
//// ---------- Reviews ----------
//@Composable
//private fun ReviewsSection(reviews: List<UserReview>, onIntent: (MediaDetailIntent) -> Unit) {
//    SectionHeader(title = stringResource(Res.string.user_reviews), action = "Write Review")
//    reviews.forEach { review ->
//        GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
//            Column(Modifier.padding(12.dp)) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Avatar(review.initials)
//                    Spacer(Modifier.width(8.dp))
//                    Column(Modifier.weight(1f)) {
//                        Text(review.username, fontWeight = FontWeight.SemiBold)
//                        Row {
//                            repeat(5) { index ->
//                                Icon(
//                                    if (index < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
//                                    contentDescription = null,
//                                    tint = MaterialTheme.colorScheme.secondary,
//                                    modifier = Modifier.size(12.dp)
//                                )
//                            }
//                        }
//                    }
//                    Text(
//                        review.daysAgo,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//                Spacer(Modifier.height(4.dp))
//                Text(
//                    review.reviewText,
//                    style = MaterialTheme.typography.bodySmall,
//                    maxLines = 3,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun Avatar(initials: String) {
//    Box(
//        modifier = Modifier.size(40.dp).clip(CircleShape)
//            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(initials, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
//    }
//}
//
//// ---------- Relations ----------
//@Composable
//private fun RelationsSection(relations: List<Relation>) {
//    SectionHeader(title = stringResource(Res.string.relations))
//    relations.forEach { rel ->
//        GlassCard(
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).clickable { },
//            onClick = {}
//        ) {
//            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
//                MediaAsyncImage(
//                    url = rel.posterUrl,
//                    contentDescription = rel.title,
//                    modifier = Modifier.width(64.dp).height(80.dp).clip(RoundedCornerShape(8.dp)),
//                    contentScale = ContentScale.Crop
//                )
//                Spacer(Modifier.width(12.dp))
//                Column {
//                    Text(
//                        rel.type,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(rel.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
//                    Text(
//                        "${rel.format} • ${rel.year}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ---------- Info Panel (Desktop) ----------
//@Composable
//private fun InfoPanel(state: MediaDetailState) {
//    GlassCard {
//        Column(Modifier.padding(16.dp)) {
//            Text(
//                stringResource(Res.string.information),
//                style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.onTertiaryContainer
//            )
//            Spacer(Modifier.height(12.dp))
//            InfoRow("Status", "Currently Airing")
//            InfoRow("Format", "TV Series")
//            InfoRow("Premiered", "Fall 2024")
//            InfoRow("Duration", "24 mins / ep")
//            InfoRow("Rating", "PG-13 (Teens)")
//            Spacer(Modifier.height(12.dp))
//            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
//            Spacer(Modifier.height(12.dp))
//            Text(
//                "Producers",
//                style = MaterialTheme.typography.labelLarge,
//                color = MaterialTheme.colorScheme.onTertiaryContainer
//            )
//            Spacer(Modifier.height(8.dp))
//            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                Surface(
//                    shape = RoundedCornerShape(4.dp),
//                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
//                ) {
//                    Text(
//                        "Arkhos Media",
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//                Surface(
//                    shape = RoundedCornerShape(4.dp),
//                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
//                ) {
//                    Text(
//                        "Global Anime Dist.",
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun InfoRow(label: String, value: String) {
//    Row(
//        Modifier.fillMaxWidth().padding(vertical = 4.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//        Text(
//            value,
//            style = MaterialTheme.typography.bodySmall,
//            fontWeight = FontWeight.SemiBold,
//            color = MaterialTheme.colorScheme.onSurface
//        )
//    }
//}
//
//// ---------- External Links ----------
//@Composable
//private fun ExternalLinksPanel(links: List<ExternalLink>, isMobile: Boolean = false) {
//    SectionHeader(title = stringResource(Res.string.watch_follow))
//    val arranged = if (isMobile) { // mobile layout: horizontal scroll
//        LazyRow(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//            items(links) { link -> ExternalLinkItem(link) }
//        }
//    } else {
//        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            links.forEach { link -> ExternalLinkItem(link) }
//        }
//    }
//}
//
//@Composable
//private fun ExternalLinkItem(link: ExternalLink) {
//    Surface(
//        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { },
//        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
//            Icon(
//                imageVector = when (link.iconType) {
//                    IconType.CRUNCHYROLL -> Icons.Outlined.Movie
//                    IconType.NETFLIX -> Icons.Outlined.Tv
//                    IconType.WEBSITE -> Icons.Outlined.Language
//                    IconType.TWITTER -> Icons.Outlined.AccountCircle
//                    else -> Icons.Outlined.Link
//                },
//                contentDescription = link.name,
//                modifier = Modifier.size(20.dp),
//                tint = MaterialTheme.colorScheme.primary
//            )
//            Spacer(Modifier.width(8.dp))
//            Text(link.name, style = MaterialTheme.typography.labelSmall)
//        }
//    }
//}
//
//// ---------- Recommendations ----------
//@Composable
//private fun RecommendationsSection(recommendations: List<MediaThumbnail>) {
//    SectionHeader(title = stringResource(Res.string.more_like_this))
//    Row(
//        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
//        horizontalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        recommendations.forEach { item ->
//            Column(Modifier.width(140.dp)) {
//                MediaAsyncImage(
//                    url = item.posterUrl,
//                    contentDescription = item.title,
//                    modifier = Modifier.aspectRatio(2f / 3f).clip(RoundedCornerShape(8.dp)),
//                    contentScale = ContentScale.Crop
//                )
//                Text(
//                    item.title,
//                    style = MaterialTheme.typography.bodySmall,
//                    fontWeight = FontWeight.SemiBold,
//                    maxLines = 1
//                )
//            }
//        }
//    }
//}
//
//// ---------- Glass Card Component ----------
//@Composable
//private fun GlassCard(
//    modifier: Modifier = Modifier,
//    onClick: (() -> Unit)? = null,
//    content: @Composable () -> Unit
//) {
//    Surface(
//        modifier = modifier,
//        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
//        shape = RoundedCornerShape(12.dp),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
//        onClick = onClick ?: {}
//    ) {
//        content()
//    }
//}
//
//// ---------- Section Header ----------
//@Composable
//private fun SectionHeader(title: String, action: String? = null) {
//    Row(
//        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
//        Spacer(Modifier.weight(1f))
//        if (action != null) {
//            TextButton(onClick = {}) {
//                Text(action, color = MaterialTheme.colorScheme.primary)
//            }
//        }
//    }
//}
//
//// ---------- String Resources (assumed in res/values/strings.xml) ----------
///*
//
//*/