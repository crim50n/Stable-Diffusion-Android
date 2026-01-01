package dev.minios.pdaiv1.presentation.screen.setup.mappers

import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.validation.ValidationResult
import dev.minios.pdaiv1.core.validation.common.CommonStringValidator
import dev.minios.pdaiv1.core.localization.R as LocalizationR

fun ValidationResult<CommonStringValidator.Error>.mapToUi(): UiText? {
    if (this.isValid) return null
    return when (validationError as CommonStringValidator.Error) {
        CommonStringValidator.Error.Empty -> LocalizationR.string.error_empty_field
    }.asUiText()
}
