package dev.minios.pdaiv1.presentation.widget.work

import android.graphics.Bitmap
import dev.minios.pdaiv1.core.model.UiText
import com.shifthackz.android.core.mvi.MviState

data class BackgroundWorkState(
    val visible: Boolean = false,
    val dismissed: Boolean = false,
    val title: UiText = UiText.empty,
    val subTitle: UiText = UiText.empty,
    val bitmap: Bitmap? = null,
    val isError: Boolean = false,
) : MviState
