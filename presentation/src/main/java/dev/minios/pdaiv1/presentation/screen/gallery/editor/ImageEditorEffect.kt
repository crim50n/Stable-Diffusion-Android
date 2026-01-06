package dev.minios.pdaiv1.presentation.screen.gallery.editor

import com.shifthackz.android.core.mvi.MviEffect

sealed interface ImageEditorEffect : MviEffect {

    data object SavedSuccessfully : ImageEditorEffect

    data object SavedAsNewImage : ImageEditorEffect
}
