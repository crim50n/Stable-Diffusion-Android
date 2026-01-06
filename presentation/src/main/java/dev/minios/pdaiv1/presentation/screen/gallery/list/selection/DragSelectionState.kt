package dev.minios.pdaiv1.presentation.screen.gallery.list.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

/**
 * State holder for drag selection in gallery grid.
 * Tracks the start and current positions of drag gesture.
 */
@Stable
class DragSelectionState {
    var isActive by mutableStateOf(false)
        private set

    var startPosition by mutableStateOf(Offset.Zero)
        private set

    var currentPosition by mutableStateOf(Offset.Zero)
        private set

    var anchorIndex by mutableStateOf<Int?>(null)
        private set

    fun startDrag(position: Offset, index: Int) {
        isActive = true
        startPosition = position
        currentPosition = position
        anchorIndex = index
    }

    fun updateDrag(position: Offset) {
        if (isActive) {
            currentPosition = position
        }
    }

    fun endDrag() {
        isActive = false
        startPosition = Offset.Zero
        currentPosition = Offset.Zero
        anchorIndex = null
    }

    fun reset() {
        endDrag()
    }
}

@Composable
fun rememberDragSelectionState(): DragSelectionState {
    return remember { DragSelectionState() }
}
