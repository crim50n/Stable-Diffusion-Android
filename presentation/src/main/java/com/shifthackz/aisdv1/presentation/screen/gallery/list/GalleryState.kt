package com.shifthackz.aisdv1.presentation.screen.gallery.list

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.MediaStoreInfo
import com.shifthackz.aisdv1.presentation.model.Modal
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
) : MviState

@Immutable
data class GalleryGridItemUi(
    val id: Long,
    val bitmap: Bitmap,
    val hidden: Boolean,
) {
    // Cache ImageBitmap to avoid recreation on each recomposition
    @Transient
    private var _imageBitmap: ImageBitmap? = null

    val imageBitmap: ImageBitmap
        get() = _imageBitmap ?: bitmap.asImageBitmap().also { _imageBitmap = it }
}
