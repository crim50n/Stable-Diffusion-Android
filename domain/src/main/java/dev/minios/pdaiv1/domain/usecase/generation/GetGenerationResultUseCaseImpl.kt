package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class GetGenerationResultUseCaseImpl(
    private val repository: GenerationResultRepository,
) : GetGenerationResultUseCase {

    override operator fun invoke(id: Long) = repository.getById(id)
}
