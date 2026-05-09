package com.ghost.zeku.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ghost.zeku.domain.model.media.MediaType
import com.ghost.zeku.domain.model.media.Media
import com.ghost.zeku.presentation.common.isDesktop
import com.ghost.zeku.presentation.common.isWideScreen
import com.ghost.zeku.presentation.common.rememberPlatformConfiguration
import com.ghost.zeku.presentation.components.hero.HeroCarousel
import com.ghost.zeku.presentation.components.hero.toHeroUiData
import com.ghost.zeku.presentation.components.media.MediaAction
import com.ghost.zeku.presentation.components.media.PaginationError
import com.ghost.zeku.presentation.components.media.list.ListCardShimmer
import com.ghost.zeku.presentation.components.media.list.MediaListCard
import com.ghost.zeku.presentation.components.media.list.toMediaListUiData
import com.ghost.zeku.presentation.components.media.poster.MediaPosterCard
import com.ghost.zeku.presentation.components.media.poster.PosterCardShimmer
import com.ghost.zeku.presentation.components.media.poster.toMediaPosterUiData
import com.ghost.zeku.presentation.components.section.*
import com.ghost.zeku.presentation.navigation.Destination
import com.ghost.zeku.presentation.viewmodel.home.HomeContract
import com.ghost.zeku.presentation.viewmodel.home.HomeViewModel
import com.ghost.zeku.utils.desktopDragScroll
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import zeku.composeapp.generated.resources.Res
import zeku.composeapp.generated.resources.no_media_found
import zeku.composeapp.generated.resources.retry
import zeku.composeapp.generated.resources.view_all


@Composable
fun MediaHomeScreen(
    mediaType: MediaType,
    viewModel: HomeViewModel = koinViewModel(),
    onNavigate: (Destination) -> Unit,
) {

    rememberPlatformConfiguration()
    val state by viewModel.state.collectAsState()
    val effect by viewModel.effects.collectAsState(null)
    val isWideScreen = rememberPlatformConfiguration().isWideScreen
    val isDesktop = rememberPlatformConfiguration().isDesktop
    LaunchedEffect(Unit) {

    }
    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        isWideScreen = isWideScreen,
        isDesktop = isDesktop,
        config = HomeUiConfig()
    )

    LaunchedEffect(mediaType) {
        // Since you already built this event, use it!
        viewModel.onEvent(HomeContract.Event.LoadHomeData(mediaType))
    }
    LaunchedEffect(mediaType) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeContract.Effect.Navigate -> {
                    onNavigate(effect.destination)
                }

                is HomeContract.Effect.ShowMessage -> TODO()
            }
        }
    }
}


@Composable
fun HomeContent(
    state: HomeContract.State,
    onEvent: (HomeContract.Event) -> Unit,
    isWideScreen: Boolean = false,
    isDesktop: Boolean = false,
    config: HomeUiConfig = HomeUiConfig()
) {
    val layoutDirection = LocalLayoutDirection.current

    val listState = rememberLazyListState()

    // Hoist the vertical paging items collection OUTSIDE the LazyColumn into a valid @Composable scope
    val verticalPagingItems = remember(state.verticalSection?.data) {
        state.verticalSection?.data ?: flowOf(PagingData.empty())
    }.collectAsLazyPagingItems()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding(),
                )
        ) {
            if (state.isLoading && state.heroItems.isEmpty() && state.horizontalSections.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // A single LazyColumn handles the entire page scroll natively
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(
                        bottom = padding.calculateBottomPadding() + Dimens.bottomContentPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        if (isWideScreen) Dimens.itemSpacingDesktop else Dimens.itemSpacingMobile
                    )
                ) {

                    // 1. HERO BANNER SECTION
                    if (state.heroItems.isNotEmpty()) {
                        item(key = "hero_banner") {
                            val heroData = remember(state.heroItems) {
                                state.heroItems.map { it.toHeroUiData() }
                            }
                            var currentPage by remember { mutableIntStateOf(0) }

                            HeroCarousel(
                                items = heroData,
                                currentPage = currentPage,
                                onPageChange = { currentPage = it },
                                onWatchClick = { item ->
                                    onEvent(
                                        HomeContract.Event.OnMediaAction(
                                            MediaAction.MediaClick(
                                                item.id,
                                                item.mediaType
                                            )
                                        )
                                    )
                                },
                                onDetailsClick = { item ->
                                    onEvent(
                                        HomeContract.Event.OnMediaAction(
                                            MediaAction.MediaClick(
                                                item.id,
                                                item.mediaType
                                            )
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isWideScreen) Dimens.heroHeightDesktop else Dimens.heroHeightMobile),
                                config = config.heroCarouselConfig,
                            )
                        }
                    }

                    // 2. DYNAMIC HORIZONTAL SECTIONS
                    state.horizontalSections.forEach { section ->
                        item(key = "horizontal_${section.categoryId}") {
                            // Collect the paging data bound to this specific section.
                            // This is safe here because `item { ... }` provides a @Composable scope!

                            val pagingItems = section.data.collectAsLazyPagingItems()


                            HorizontalMediaSection(
                                title = section.title,
                                categoryId = section.categoryId,
                                items = pagingItems,
                                config = config,
                                isDesktop = isDesktop,
                                onEvent = onEvent
                            )
                        }
                    }

                    // 3. INFINITE VERTICAL SECTION
                    state.verticalSection?.let { section ->
                        item(key = "vertical_header_${section.categoryId}") {
                            SectionHeader(
                                title = section.title,
                                action = stringResource(Res.string.view_all),
                                onAction = {
                                    onEvent(HomeContract.Event.OnViewAllClick(section.categoryId, section.title))
                                },
                                modifier = Modifier.padding(horizontal = Dimens.paddingMedium)
                            )
                        }

                        // Let LazyColumn naturally handle the heavy vertical list using the hoisted items
                        // ==============================
                        // VERTICAL LIST
                        // ==============================
                        items(
                            count = verticalPagingItems.itemCount,
                            key = { index ->
                                verticalPagingItems[index]?.id ?: "placeholder_$index"
                            }
                        ) { index ->

                            val media = verticalPagingItems[index]

                            if (media != null) {
                                MediaListCard(
                                    data = media.toMediaListUiData(),
                                    layout = config.listCardVariant,
                                    config = config.listCardConfig,
                                    onAction = { onEvent(HomeContract.Event.OnMediaAction(it)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Dimens.paddingMedium)
                                )
                            } else {
                                // ✨ Placeholder (while paging loads)
                                ListCardShimmer(config.listCardConfig)
                            }
                        }


                        if (
                            verticalPagingItems.loadState.refresh is LoadState.NotLoading &&
                            verticalPagingItems.itemCount == 0
                        ) {
                            item {
                                SectionEmptyState(
                                    text = stringResource(Res.string.no_media_found)
                                )
                            }
                        }

                        // ==============================
                        // LOAD STATES
                        // ==============================
                        verticalPagingItems.apply {

                            when {

                                // 🔄 FIRST LOAD
                                loadState.refresh is LoadState.Loading -> {
                                    item {
                                        Column {
                                            repeat(5) {
                                                ListCardShimmer(config.listCardConfig)
                                            }
                                        }
                                    }
                                }

                                // ❌ FIRST LOAD ERROR
                                loadState.refresh is LoadState.Error -> {
                                    val error = loadState.refresh as LoadState.Error

                                    item {
                                        FullScreenError(
                                            error = error.error.cause ?: Exception("Something went wrong"),
                                            onRetry = { retry() }
                                        )
                                    }
                                }

                                // ⬇️ PAGINATION LOADING
                                loadState.append is LoadState.Loading -> {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }

                                // ❌ PAGINATION ERROR
                                loadState.append is LoadState.Error -> {
                                    item {
                                        val error = loadState.append as LoadState.Error
                                        PaginationError(
                                            error = error.error,
                                            onRetry = { retry() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizontalMediaSection(
    title: String,
    categoryId: String,
    items: LazyPagingItems<Media>,
    config: HomeUiConfig,
    isDesktop: Boolean,
    onEvent: (HomeContract.Event) -> Unit
) {
    val dimens = Dimens

    val refreshState = items.loadState.refresh
    val appendState = items.loadState.append

    val isInitialLoading = refreshState is LoadState.Loading
    val isError = refreshState is LoadState.Error
    val isEmpty = items.itemCount == 0 && refreshState is LoadState.NotLoading

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(horizontal = dimens.paddingLarge),
        verticalArrangement = Arrangement.spacedBy(dimens.paddingMedium)
    ) {

        // 🔹 Header
        SectionHeader(
            title = title,
            action = stringResource(Res.string.view_all),
            onAction = {
                onEvent(HomeContract.Event.OnViewAllClick(categoryId, title))
            },
        )

        // =========================
        // 1. INITIAL LOADING
        // =========================
        if (isInitialLoading) {
            SectionLoadingShimmer(
                layout = SectionLayout.HorizontalRow(),
                count = 10
            )
            return@Column
        }

        // =========================
        // 2. ERROR (FULL)
        // =========================
        if (isError && items.itemCount == 0) {
            val error = (refreshState as LoadState.Error).error

            SectionErrorState(
                message = error.localizedMessage ?: "Something went wrong",
                onRetry = { items.retry() }
            )
            return@Column
        }

        // =========================
        // 3. EMPTY STATE
        // =========================
        if (isEmpty) {
            SectionEmptyState(
                text = stringResource(Res.string.no_media_found)
            )
            return@Column
        }

        // =========================
        // 4. CONTENT
        // =========================
        val carouselState = rememberCarouselState { items.itemCount }

        HorizontalUncontainedCarousel(
            state = carouselState,
            itemWidth = 210.dp,
            itemSpacing = 6.dp,
            modifier = Modifier
                .then(if (isDesktop) Modifier.desktopDragScroll(carouselState) else Modifier)
                .pointerHoverIcon(
                    if (isDesktop) PointerIcon.Hand else PointerIcon.Default
                )
        ) { index ->

            val item = items[index]




            if (item != null) {
                MediaPosterCard(
                    data = item.toMediaPosterUiData(),
                    layout = config.posterLayout,
                    config = config.posterConfig,
                    onAction = { onEvent(HomeContract.Event.OnMediaAction(it)) },
                    modifier = Modifier.padding(10.dp)
                )
            } else {
                // 🔹 Placeholder while paging loads
                PosterCardShimmer(config = config.posterConfig)
            }
        }

        // =========================
        // 5. APPEND STATES (IMPORTANT)
        // =========================

        when (appendState) {
            is LoadState.Loading -> {
                // small inline loader
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) {
                        PosterCardShimmer(config = config.posterConfig)
                    }
                }
            }

            is LoadState.Error -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { items.retry() }) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }

            else -> Unit
        }
    }
}


