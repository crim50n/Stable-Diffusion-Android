package dev.minios.pdaiv1.presentation.screen.gallery.editor

import com.shifthackz.android.core.mvi.MviIntent

sealed interface ImageEditorIntent : MviIntent {

    data object NavigateBack : ImageEditorIntent

    data object RotateLeft : ImageEditorIntent

    data object RotateRight : ImageEditorIntent

    data object FlipHorizontal : ImageEditorIntent

    data object FlipVertical : ImageEditorIntent

    data class UpdateBrightness(val value: Float) : ImageEditorIntent

    data class UpdateContrast(val value: Float) : ImageEditorIntent

    data class UpdateSaturation(val value: Float) : ImageEditorIntent

    data object ResetFilters : ImageEditorIntent

    data object Save : ImageEditorIntent

    data object SaveAs : ImageEditorIntent

    data object DismissDialog : ImageEditorIntent

    enum class Tool {
        ROTATE, ADJUST;
    }

    data class SelectTool(val tool: Tool) : ImageEditorIntent
}
