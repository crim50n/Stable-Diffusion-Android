package dev.minios.pdaiv1.core.validation.path

import dev.minios.pdaiv1.core.validation.ValidationResult

interface FilePathValidator {

    operator fun invoke(input: String?): ValidationResult<Error>

    sealed interface Error {
        data object Empty : Error
        data object Invalid : Error
    }
}
