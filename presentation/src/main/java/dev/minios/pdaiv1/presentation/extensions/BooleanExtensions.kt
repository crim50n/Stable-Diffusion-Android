package dev.minios.pdaiv1.presentation.extensions

import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.localization.R as LocalizationR

fun Boolean.mapToUi(): UiText = (if (this) LocalizationR.string.yes else LocalizationR.string.no).asUiText()
