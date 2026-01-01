package dev.minios.pdaiv1.presentation.model

import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.core.model.UiText
import com.shifthackz.android.core.mvi.MviState

sealed interface ErrorState : MviState {

    data object None : ErrorState

    data object Generic : ErrorState

    @Immutable
    data class WithMessage(val message: UiText) : ErrorState
}
