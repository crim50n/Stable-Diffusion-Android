package dev.minios.pdaiv1.presentation.screen.logger

import com.shifthackz.android.core.mvi.MviEffect

sealed interface LoggerEffect : MviEffect {
    data class CopyToClipboard(val text: String) : LoggerEffect
    data class ShareLog(val text: String) : LoggerEffect
}
