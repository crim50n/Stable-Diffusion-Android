package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class ToggleImageVisibilityUseCaseImpl(
    private val repository: GenerationResultRepository,
) : ToggleImageVisibilityUseCase {

    override fun invoke(id: Long) = repository.toggleVisibility(id)
}
