package dev.minios.pdaiv1.presentation.screen.gallery.editor

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.shifthackz.android.core.mvi.MviState
import dev.minios.pdaiv1.presentation.model.Modal

@Immutable
data class ImageEditorState(
    val originalBitmap: Bitmap? = null,
    val editedBitmap: Bitmap? = null,
    val rotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val brightness: Float = 0f, // -1 to 1
    val contrast: Float = 1f, // 0 to 2
    val saturation: Float = 1f, // 0 to 2
    val selectedTool: ImageEditorIntent.Tool = ImageEditorIntent.Tool.ADJUST,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val screenModal: Modal = Modal.None,
    val hasChanges: Boolean = false,
) : MviState {

    val displayBitmap: Bitmap?
        get() = editedBitmap ?: originalBitmap

    fun withFiltersApplied(): ImageEditorState = copy(
        hasChanges = rotation != 0f ||
                flipHorizontal ||
                flipVertical ||
                brightness != 0f ||
                contrast != 1f ||
                saturation != 1f
    )
}
