package dev.minios.pdaiv1.presentation.screen.inpaint

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.presentation.model.InPaintModel
import dev.minios.pdaiv1.presentation.model.Modal
import com.shifthackz.android.core.mvi.MviState
import dev.minios.pdaiv1.core.localization.R as LocalizationR
import dev.minios.pdaiv1.presentation.R as PresentationR

@Immutable
data class InPaintState(
    val screenModal: Modal = Modal.None,
    val bitmap: Bitmap? = null,
    val selectedTab: Tab = Tab.IMAGE,
    val size: Int = 16,
    val model: InPaintModel = InPaintModel(),
    val zoomScale: Float = 1f,
    val zoomOffsetX: Float = 0f,
    val zoomOffsetY: Float = 0f,
    val isDrawing: Boolean = false,
) : MviState {

    val isZoomed: Boolean
        get() = zoomScale > 1.05f

    enum class Tab(
        @StringRes val label: Int,
        @DrawableRes val iconRes: Int,
    ) {
        IMAGE(
            LocalizationR.string.in_paint_tab_1,
            PresentationR.drawable.ic_image,
        ),
        FORM(
            LocalizationR.string.in_paint_tab_2,
            PresentationR.drawable.ic_image,
        );
    }
}
