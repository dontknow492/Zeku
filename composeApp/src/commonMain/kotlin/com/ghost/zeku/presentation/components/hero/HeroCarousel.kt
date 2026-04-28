package com.ghost.zeku.presentation.components.hero


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.ghost.zeku.presentation.common.isDesktop
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroCarousel(
    modifier: Modifier = Modifier,
    items: List<MediaHeroUiData>,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onWatchClick: (MediaHeroUiData) -> Unit,
    onDetailsClick: (MediaHeroUiData) -> Unit,
    config: HeroCarouselConfig? = null
) {
    val isDesktop = rememberPlatformConfiguration().isDesktop
    val resolved = config ?: HeroCarouselDefaults.config(isDesktop)

    val state = rememberCarouselState(initialItem = 0) { items.count() }
    val pagerState = rememberPagerState { items.count() }

    val coroutineScope = rememberCoroutineScope()
    var isUserInteracting by remember { mutableStateOf(false) }


    // Notify parent of page changes
    LaunchedEffect(pagerState.currentPage) {
        if (currentPage != pagerState.currentPage) {
            onPageChange(pagerState.currentPage)
        }
    }

    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(
            currentPage,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
    }

//    // Auto-scroll logic
    if (resolved.enableAutoScroll) {
        LaunchedEffect(currentPage, pagerState.currentPage) {
            delay(resolved.autoScrollDuration.milliseconds)
            // Use animation to smoothly slide to the next page, loop back to 0 at the end
            val nextPage = (pagerState.currentPage + 1) % items.size
            onPageChange(nextPage)
        }
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = resolved.peek),
            pageSpacing = resolved.pageSpacing,
            modifier = Modifier.fillMaxWidth(),
            beyondViewportPageCount = resolved.beyondViewportPageCount,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(1)
            )
        ) { page ->
            val item = items[page]


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


            MediaHeroBanner(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        this.translationX = translationX
                    },
                data = item,
                config = resolved.itemConfig,
                onWatchClick = onWatchClick,
                onDetailsClick = onDetailsClick,
            )

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
@Preview(showBackground = true)
@Composable
fun HeroCarouselPreview() {
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
        ),
        MediaHeroUiData(
            id = 3,
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
        Box(modifier = Modifier.fillMaxSize()) {
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