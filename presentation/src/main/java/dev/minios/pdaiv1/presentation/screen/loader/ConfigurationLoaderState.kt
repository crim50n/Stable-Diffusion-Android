package dev.minios.pdaiv1.presentation.screen.loader

import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.core.model.UiText
import com.shifthackz.android.core.mvi.MviState

interface ConfigurationLoaderState : MviState {

    @Immutable
    data class StatusNotification(val statusNotification: UiText) : ConfigurationLoaderState
}
