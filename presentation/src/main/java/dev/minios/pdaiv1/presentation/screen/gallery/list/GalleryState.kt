package dev.minios.pdaiv1.presentation.screen.gallery.list

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dev.minios.pdaiv1.domain.entity.Grid
import dev.minios.pdaiv1.domain.entity.MediaStoreInfo
import dev.minios.pdaiv1.presentation.model.Modal
import com.shifthackz.android.core.mvi.MviState

@Immutable
data class GalleryState(
    val screenModal: Modal = Modal.None,
    val mediaStoreInfo: MediaStoreInfo = MediaStoreInfo(),
    val dropdownMenuShow: Boolean = false,
    val selectionMode: Boolean = false,
    val selection: List<Long> = emptyList(),
    val grid: Grid = Grid.Fixed2,
    val scrollToItemIndex: Int? = null,
    // Immich-style: selected item shown as overlay, null = gallery grid visible
    val selectedItemId: Long? = null,
    val selectedItemIndex: Int? = null,
    // Immich-style lazy loading: all IDs loaded at startup, bitmaps loaded on demand
    val allIds: List<Long> = emptyList(),
    val hiddenIds: Set<Long> = emptySet(),
    val likedIds: Set<Long> = emptySet(),
    val thumbnailCache: Map<Long, Bitmap> = emptyMap(),
    val blurHashCache: Map<Long, String> = emptyMap(), // BlurHash strings for placeholders
    val loadingIds: Set<Long> = emptySet(), // IDs currently being loaded
    val isInitialLoading: Boolean = true, // True until IDs are loaded
) : MviState {
    val totalItems: Int get() = allIds.size
}

/**
 * Lightweight item for grid display - bitmap loaded lazily
 */
@Immutable
data class GalleryGridItemUi(
    val id: Long,
    val bitmap: Bitmap?, // Nullable for lazy loading (Immich-style)
    val hidden: Boolean,
    val liked: Boolean = false,
    val blurHash: String = "", // BlurHash for placeholder while loading
) {
    // Cache ImageBitmap to avoid recreation on each recomposition
    @Transient
    private var _imageBitmap: ImageBitmap? = null

    val imageBitmap: ImageBitmap?
        get() = bitmap?.let { bmp ->
            _imageBitmap ?: bmp.asImageBitmap().also { _imageBitmap = it }
        }
}
