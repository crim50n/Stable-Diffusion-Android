package dev.minios.pdaiv1.presentation.widget.scaffold

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A Scaffold-like composable that supports collapsing TopBar on scroll.
 * Works like Gallery - content scrolls under TopBar, TopBar slides up to reveal content.
 *
 * @param topBarContent The content for the top bar
 * @param bottomBar Optional bottom bar content
 * @param topBarHeight Height of the top bar content (without status bar)
 * @param bottomNavBarHeight Height of home navigation bar
 * @param bottomToolbarHeight Height of screen's own bottom toolbar
 * @param scrollState Optional external scroll state
 * @param contentScrollable Content that will be wrapped in a scrollable Column with proper spacing
 */
@Composable
fun CollapsibleScaffold(
    topBarContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    topBarHeight: Dp = 72.dp,
    bottomNavBarHeight: Dp = 80.dp,
    bottomToolbarHeight: Dp = 80.dp,
    scrollState: ScrollState? = null,
    contentScrollable: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val totalTopBarHeight = topBarHeight + statusBarHeightDp
    val topBarHeightPx = with(density) { totalTopBarHeight.toPx() }

    // UI offset: 0f = fully visible, 1f = fully hidden
    var uiOffset by remember { mutableFloatStateOf(0f) }

    // Animate the offset for smoother transitions
    val animatedOffset by animateFloatAsState(
        targetValue = uiOffset,
        animationSpec = tween(durationMillis = 150),
        label = "topbar_offset"
    )

    // NestedScrollConnection to intercept scroll events from any child
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = (uiOffset - delta / topBarHeightPx).coerceIn(0f, 1f)
                uiOffset = newOffset
                return Offset.Zero
            }
        }
    }

    // Internal scroll state for content
    val internalScrollState = scrollState ?: rememberScrollState()

    // Reset topbar when at top
    LaunchedEffect(internalScrollState) {
        snapshotFlow { internalScrollState.value }
            .collect { scrollValue ->
                if (scrollValue < 50) {
                    uiOffset = 0f
                }
            }
    }

    val totalBottomPadding = bottomNavBarHeight + bottomToolbarHeight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        // Main content - scrollable Column with internal spacing (like Gallery's contentPadding)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(internalScrollState)
                .padding(bottom = totalBottomPadding)
        ) {
            // Internal spacer for TopBar (like Gallery's contentPadding top)
            Spacer(modifier = Modifier.height(totalTopBarHeight))
            // Actual content
            contentScrollable()
        }

        // TopBar as overlay - slides up based on scroll
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = -topBarHeightPx * animatedOffset
                }
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            topBarContent()
        }

        // Bottom bar - positioned above the navigation bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomNavBarHeight)
        ) {
            bottomBar()
        }
    }
}

/**
 * Overload that accepts PaddingValues for compatibility.
 * Content must apply padding itself internally (not as outer container padding).
 */
@Composable
fun CollapsibleScaffold(
    topBarContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    topBarHeight: Dp = 72.dp,
    bottomNavBarHeight: Dp = 80.dp,
    bottomToolbarHeight: Dp = 80.dp,
    scrollState: ScrollState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val totalTopBarHeight = topBarHeight + statusBarHeightDp
    val topBarHeightPx = with(density) { totalTopBarHeight.toPx() }

    var uiOffset by remember { mutableFloatStateOf(0f) }

    val animatedOffset by animateFloatAsState(
        targetValue = uiOffset,
        animationSpec = tween(durationMillis = 150),
        label = "topbar_offset"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = (uiOffset - delta / topBarHeightPx).coerceIn(0f, 1f)
                uiOffset = newOffset
                return Offset.Zero
            }
        }
    }

    if (scrollState != null) {
        LaunchedEffect(scrollState) {
            snapshotFlow { scrollState.value }
                .collect { scrollValue ->
                    if (scrollValue < 50) {
                        uiOffset = 0f
                    }
                }
        }
    }

    val totalBottomPadding = bottomNavBarHeight + bottomToolbarHeight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        // Content with padding values - content must handle scrolling
        content(
            PaddingValues(
                top = totalTopBarHeight,
                bottom = totalBottomPadding
            )
        )

        // TopBar as overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = -topBarHeightPx * animatedOffset
                }
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            topBarContent()
        }

        // Bottom bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomNavBarHeight)
        ) {
            bottomBar()
        }
    }
}
