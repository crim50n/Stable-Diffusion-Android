package dev.minios.pdaiv1.core.validation.common

import dev.minios.pdaiv1.core.validation.ValidationResult

interface CommonStringValidator {

    operator fun invoke(input: String?) : ValidationResult<Error>

    sealed interface Error {
        data object Empty : Error
    }
}
