package dev.minios.pdaiv1.presentation.screen.gallery.list.selection

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.geometry.Offset
import kotlin.math.max
import kotlin.math.min

/**
 * Utility functions for drag selection in gallery grid.
 */
object DragSelectionUtils {

    /**
     * Calculates the range of indices between anchor and current position.
     * Returns indices in the range [minIndex, maxIndex].
     */
    fun calculateSelectedRange(
        anchorIndex: Int,
        currentIndex: Int,
    ): IntRange {
        val minIndex = min(anchorIndex, currentIndex)
        val maxIndex = max(anchorIndex, currentIndex)
        return minIndex..maxIndex
    }

    /**
     * Estimates the item index at a given position in the grid.
     * This is an approximation based on grid layout.
     *
     * @param position The position in pixels
     * @param gridState The lazy grid state
     * @param columns Number of columns in the grid
     * @param itemSize Approximate size of each item (including spacing)
     * @param contentPadding Content padding of the grid
     * @return Estimated item index, or null if outside grid bounds
     */
    fun estimateItemIndexAtPosition(
        position: Offset,
        gridState: LazyGridState,
        columns: Int,
        itemSize: Float,
        contentPadding: Float,
    ): Int? {
        if (position.x < contentPadding || position.y < contentPadding) {
            return null
        }

        val adjustedX = position.x - contentPadding
        val adjustedY = position.y - contentPadding

        val column = (adjustedX / itemSize).toInt().coerceIn(0, columns - 1)
        val row = (adjustedY / itemSize).toInt()

        val scrollOffset = gridState.firstVisibleItemScrollOffset
        val firstVisibleRow = gridState.firstVisibleItemIndex / columns
        val actualRow = firstVisibleRow + row + (scrollOffset / itemSize).toInt()

        return actualRow * columns + column
    }

    /**
     * Determines if auto-scroll should be triggered based on drag position.
     * Returns scroll direction: negative for up, positive for down, zero for no scroll.
     */
    fun getAutoScrollDirection(
        dragPosition: Offset,
        viewportHeight: Float,
        edgeThreshold: Float = 100f,
    ): Float {
        return when {
            dragPosition.y < edgeThreshold -> -1f
            dragPosition.y > viewportHeight - edgeThreshold -> 1f
            else -> 0f
        }
    }
}
