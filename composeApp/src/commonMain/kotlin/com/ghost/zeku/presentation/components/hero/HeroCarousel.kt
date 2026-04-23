package com.ghost.zeku.presentation.components.hero


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds

//@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HeroCarousel(
    items: List<MediaHeroUiData>,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onWatchClick: (MediaHeroUiData) -> Unit,
    onDetailsClick: (MediaHeroUiData) -> Unit,
    modifier: Modifier = Modifier,
    config: HeroCarouselConfig? = null
) {
    if (items.isEmpty()) return

    val platform = rememberPlatformConfiguration()
    val isDesktop = platform.screenSizeDp.width > 600.dp
    val resolved = config ?: HeroCarouselDefaults.config(isDesktop)

    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    // Track user interaction to pause auto-scroll
    var isUserInteracting by remember { mutableStateOf(false) }
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Reset user interaction flag after dragging stops
    LaunchedEffect(isDragged) {
        if (isDragged) {
            isUserInteracting = true
        } else {
            delay(3000) // Wait 3 seconds after user stops interacting
            isUserInteracting = false
        }
    }

    // 1. Synchronize external state with internal pager
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage && !isUserInteracting) {
            pagerState.animateScrollToPage(
                page = currentPage,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        }
    }

    // 2. Update external page when pager changes (only when not animating from external)
    LaunchedEffect(pagerState.currentPage, isDragged) {
        if (!isDragged && !isUserInteracting) {
            onPageChange(pagerState.currentPage)
        }
    }

    // 3. AUTO-SCROLL (pauses on user interaction)
    LaunchedEffect(pagerState.currentPage, isUserInteracting, resolved.enableAutoScroll) {
        if (!resolved.enableAutoScroll || isUserInteracting || items.size <= 1) return@LaunchedEffect

        delay(resolved.autoScrollDuration.milliseconds)
        val nextPage = (pagerState.currentPage + 1) % items.size

        pagerState.animateScrollToPage(
            nextPage,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // 4. Animated Pager with visible prev/next cards
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = resolved.peek),
            pageSpacing = resolved.pageSpacing,
            beyondViewportPageCount = resolved.beyondViewportPageCount,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(1)
            )
        ) { page ->
            // Calculate page offset for animations (shows prev/next cards)
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            // Scale animation for depth effect
            val scale = lerp(resolved.scaleMin, 1f, 1f - pageOffset.coerceIn(0f, 1f))

            // Alpha fade for edges
            val alpha = lerp(resolved.alphaMin, 1f, 1f - pageOffset.coerceIn(0f, 1f))

            // Parallax translation
            val translationX = if (resolved.enableParallax) {
                pageOffset * resolved.parallaxOffset
            } else 0f

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        this.translationX = translationX
                    }
                    .clip(RoundedCornerShape(if (isDesktop) 20.dp else 12.dp))
            ) {
                MediaHeroBanner(
                    data = items[page],
                    onWatchClick = { onWatchClick(items[page]) },
                    onDetailsClick = { onDetailsClick(items[page]) }
                )
            }
        }

        // 5. Edge fade gradients for desktop (creates smooth transition effect)
        if (isDesktop && resolved.showEdgeGradients) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {} // Allows clicks to pass through
            ) {
                // Left gradient fade
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 100f
                            )
                        )
                )

                // Right gradient fade
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
                        .align(Alignment.CenterEnd)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                ),
                                startX = 0f,
                                endX = 100f
                            )
                        )
                )
            }
        }

        // 6. Page Indicators (desktop only, cleaner look)
        if (items.size > 1 && resolved.showIndicators) {
            CarouselIndicators(
                totalDots = items.size,
                selectedIndex = pagerState.currentPage,
                onIndicatorClick = { index ->
                    coroutineScope.launch {
                        isUserInteracting = true
                        pagerState.animateScrollToPage(index)
                        delay(3000.milliseconds)
                        isUserInteracting = false
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isDesktop) 24.dp else 16.dp)
            )
        }
    }
}


@Composable
private fun CarouselIndicators(
    totalDots: Int,
    selectedIndex: Int,
    onIndicatorClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalDots) {
            val isSelected = i == selectedIndex

            val width by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "indicatorWidth"
            )

            val color by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                animationSpec = tween(300),
                label = "indicatorColor"
            )

            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(width)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
                    .clickable { onIndicatorClick(i) }
            )
        }
    }
}

// ============================================================================
// PREVIEW
// ============================================================================
@Preview(showBackground = true, widthDp = 900, heightDp = 500)
@Composable
private fun HeroCarouselPreview() {
    val items = listOf(
        MediaHeroUiData(
            id = 1,
            title = "Solo Leveling",
            bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/151807-ngjsN8vJ8p83.jpg",
            coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx151807-m1gX3iqITmI6.png",
            description = "A weak hunter becomes the strongest after a mysterious quest.",
            genres = listOf("Action", "Fantasy"),
            badgeText = "Releasing"
        ),
        MediaHeroUiData(
            id = 2,
            title = "Jujutsu Kaisen",
            bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/145139-F2ZUCz7m4n5F.jpg",
            coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx145139-2DqjM6XOwTb6.jpg",
            description = "A boy swallows a cursed object and fights evil spirits.",
            genres = listOf("Action", "Supernatural"),
            badgeText = "Trending"
        )
    )
    var currentPage by remember { mutableIntStateOf(0) }
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            HeroCarousel(
                items = items,
                currentPage = currentPage,
                onPageChange = { currentPage = it },
                onWatchClick = {},
                onDetailsClick = {},
            )
        }
    }
}