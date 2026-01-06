@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.minios.pdaiv1.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal for SharedTransitionScope - used for Immich-style hero animations
 * between gallery grid and detail view.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * CompositionLocal for AnimatedVisibilityScope - needed for sharedElement modifier
 */
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * CompositionLocal for controlling bottom navigation bar visibility.
 * When true, the bottom navigation bar in HomeNavigationScreen should be hidden.
 * This is used during full-screen image viewing in gallery detail.
 */
val LocalHideBottomNavigation = compositionLocalOf { false }

/**
 * Callback to update the bottom navigation visibility state.
 * Call with true to hide, false to show.
 */
val LocalSetHideBottomNavigation = compositionLocalOf<((Boolean) -> Unit)?> { null }

/**
 * Wrapper that provides SharedTransitionLayout scope to the entire navigation tree.
 * This enables shared element transitions between screens.
 */
@Composable
fun SharedTransitionProvider(
    content: @Composable SharedTransitionScope.() -> Unit
) {
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            content()
        }
    }
}

/**
 * Helper to create shared element key for gallery items
 */
fun galleryItemSharedKey(itemId: Long) = "gallery_item_$itemId"

/**
 * Helper to create shared element key for gallery item image
 */
fun galleryImageSharedKey(itemId: Long) = "gallery_image_$itemId"
