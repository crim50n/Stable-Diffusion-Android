package dev.minios.pdaiv1.presentation.screen.setup.mappers

import dev.minios.pdaiv1.core.localization.R
import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.validation.ValidationResult
import dev.minios.pdaiv1.core.validation.path.FilePathValidator

fun ValidationResult<FilePathValidator.Error>.mapToUi(): UiText? {
    if (this.isValid) return null
    return when (validationError as FilePathValidator.Error) {
        FilePathValidator.Error.Empty -> R.string.error_empty_field
        FilePathValidator.Error.Invalid -> R.string.error_invalid
    }.asUiText()
}
