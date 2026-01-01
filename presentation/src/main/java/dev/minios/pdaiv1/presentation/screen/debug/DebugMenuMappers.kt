package dev.minios.pdaiv1.presentation.screen.debug

import dev.minios.pdaiv1.core.common.schedulers.SchedulersToken
import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.localization.R as LocalizationR

fun SchedulersToken.mapToUi(): UiText = when (this) {
    SchedulersToken.MAIN_THREAD -> LocalizationR.string.scheduler_main
    SchedulersToken.IO_THREAD -> LocalizationR.string.scheduler_io
    SchedulersToken.COMPUTATION -> LocalizationR.string.scheduler_computation
    SchedulersToken.SINGLE_THREAD -> LocalizationR.string.scheduler_single_thread
}.asUiText()
