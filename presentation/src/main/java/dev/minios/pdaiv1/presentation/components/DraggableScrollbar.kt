package dev.minios.pdaiv1.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Draggable scrollbar for fast navigation in gallery grid.
 * Shows current scroll position and allows quick scrolling by dragging.
 */
@Composable
fun DraggableScrollbar(
    lazyGridState: LazyGridState,
    totalItems: Int,
    columns: Int,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var containerHeight by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragThumbOffset by remember { mutableFloatStateOf(0f) }

    val thumbHeightDp = 48.dp
    val thumbHeight = with(density) { thumbHeightDp.toPx() }

    // Show scrollbar only when there are enough items
    val showScrollbar by remember(totalItems, columns) {
        derivedStateOf { totalItems > columns * 3 }
    }

    // Calculate scroll progress (0 to 1) based on grid state
    val scrollProgress by remember(lazyGridState) {
        derivedStateOf {
            if (totalItems == 0 || columns == 0) return@derivedStateOf 0f

            val totalRows = (totalItems + columns - 1) / columns
            val firstVisibleRow = lazyGridState.firstVisibleItemIndex / columns

            if (totalRows <= 1) 0f
            else (firstVisibleRow.toFloat() / (totalRows - 1).coerceAtLeast(1))
                .coerceIn(0f, 1f)
        }
    }

    AnimatedVisibility(
        visible = showScrollbar,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(24.dp)
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .onSizeChanged { containerHeight = it.height.toFloat() }
                .pointerInput(totalItems, columns) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            // Initialize drag offset from current scroll position
                            val maxThumbOffset = (containerHeight - thumbHeight).coerceAtLeast(0f)
                            dragThumbOffset = scrollProgress * maxThumbOffset
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()

                            val maxThumbOffset = (containerHeight - thumbHeight).coerceAtLeast(0f)
                            
                            // Update drag offset directly
                            dragThumbOffset = (dragThumbOffset + dragAmount)
                                .coerceIn(0f, maxThumbOffset)

                            val newProgress = if (maxThumbOffset > 0) {
                                dragThumbOffset / maxThumbOffset
                            } else 0f

                            val totalRows = (totalItems + columns - 1) / columns
                            val targetRow = (newProgress * (totalRows - 1)).roundToInt()
                            val targetIndex = (targetRow * columns).coerceIn(0, totalItems - 1)

                            coroutineScope.launch {
                                lazyGridState.scrollToItem(targetIndex)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            // Track background
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
            )

            // Calculate thumb offset - use drag offset when dragging, scroll progress otherwise
            val maxThumbOffset = (containerHeight - thumbHeight).coerceAtLeast(0f)
            val thumbOffset = if (isDragging) {
                dragThumbOffset.roundToInt()
            } else {
                (scrollProgress * maxThumbOffset).roundToInt()
            }

            // Thumb
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, thumbOffset) }
                    .width(16.dp)
                    .height(thumbHeightDp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isDragging) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        }
                    )
            )
        }
    }
}
