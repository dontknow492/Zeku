package com.ghost.zeku.presentation.common

import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedPager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    horizontalPadding: Dp,
    beyondViewportPageCount: Int = 1,
    pageSpacing: Dp = 0.dp,
    flingBehavior: TargetedFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1)
    ),
    content: @Composable PagerScope.(page: Int) -> Unit
) {

    HorizontalPager(
        state = pagerState,
        modifier = modifier, // don't override
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        pageSpacing = pageSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        flingBehavior = flingBehavior
    ) { page ->
        this@HorizontalPager.content(page)
    }
}