package dev.minios.pdaiv1.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.minios.pdaiv1.core.model.asString
import dev.minios.pdaiv1.presentation.model.NavItem
import dev.minios.pdaiv1.presentation.navigation.LocalHideBottomNavigation
import dev.minios.pdaiv1.presentation.navigation.LocalSetHideBottomNavigation
import dev.minios.pdaiv1.presentation.widget.connectivity.ConnectivityComposable
import dev.minios.pdaiv1.presentation.widget.item.NavigationItemIcon
import kotlinx.coroutines.launch

@Composable
fun HomeNavigationScreen(
    navItems: List<NavItem> = emptyList(),
) {
    require(navItems.isNotEmpty()) { "navItems collection must not be empty." }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { navItems.size }
    )

    // State for controlling bottom navigation visibility (used by gallery detail overlay)
    var hideBottomNavigation by remember { mutableStateOf(false) }

    // Animate bottom navigation offset for smooth hide/show without layout changes
    val bottomNavOffset by animateFloatAsState(
        targetValue = if (hideBottomNavigation) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "bottom_nav_offset"
    )

    // Bottom navigation bar height (approximate)
    val bottomNavHeight = 80.dp
    val density = LocalDensity.current
    val bottomNavHeightPx = with(density) { bottomNavHeight.toPx() }

    CompositionLocalProvider(
        LocalHideBottomNavigation provides hideBottomNavigation,
        LocalSetHideBottomNavigation provides { hide -> hideBottomNavigation = hide },
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            bottomBar = {
                // Use graphicsLayer for smooth hide/show without layout changes
                NavigationBar(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = bottomNavHeightPx * bottomNavOffset
                            alpha = 1f - bottomNavOffset
                        },
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    navItems.forEachIndexed { index, item ->
                        val selected = pagerState.currentPage == index
                        NavigationBarItem(
                            selected = selected,
                            label = {
                                Text(
                                    text = item.name.asString(),
                                    color = LocalContentColor.current,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors().copy(
                                selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                            ),
                            icon = { NavigationItemIcon(item.icon) },
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                        )
                    }
                }
            },
            content = { _ ->
                // All screens use CollapsibleScaffold which manages its own layout
                // Bottom padding is handled by each screen individually
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Hide connectivity widget in fullscreen mode
                        AnimatedVisibility(visible = !hideBottomNavigation) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center,
                            ) {
                                ConnectivityComposable()
                            }
                        }
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = !hideBottomNavigation, // Disable swipe in fullscreen mode
                        ) { page ->
                            navItems[page].content?.invoke()
                        }
                    }
                }
            }
        )
    }
}
