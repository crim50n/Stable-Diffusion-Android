package dev.minios.pdaiv1.presentation.screen.inpaint

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Path
import dev.minios.pdaiv1.presentation.model.InPaintModel
import dev.minios.pdaiv1.presentation.model.Modal
import com.shifthackz.android.core.mvi.MviIntent

sealed interface InPaintIntent : MviIntent {

    data object NavigateBack : InPaintIntent

    data class SelectTab(val tab: InPaintState.Tab) : InPaintIntent

    data class ChangeCapSize(val size: Int) : InPaintIntent

    data class DrawPath(val path: Path) : InPaintIntent

    data class DrawPathBmp(val bitmap: Bitmap?) : InPaintIntent

    enum class Action : InPaintIntent {
        Undo, Clear, ToggleDrawing, ResetZoom;
    }

    data class UpdateZoomScale(val scale: Float) : InPaintIntent

    data class UpdateZoomOffset(val offsetX: Float, val offsetY: Float) : InPaintIntent

    sealed interface Update : InPaintIntent {

        data class MaskBlur(val value: Int) : Update

        data class OnlyMaskedPadding(val value: Int) : Update

        data class MaskMode(val value: InPaintModel.MaskMode) : Update

        data class MaskContent(val value: InPaintModel.MaskContent) : Update

        data class Area(val value: InPaintModel.Area) : Update
    }

    sealed interface ScreenModal : InPaintIntent {

        data class Show(val modal: Modal) : ScreenModal

        data object Dismiss : ScreenModal
    }
}
