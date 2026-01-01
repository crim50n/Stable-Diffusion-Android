package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class SaveGenerationResultUseCaseImpl(
    private val repository: GenerationResultRepository,
) : SaveGenerationResultUseCase {

    override fun invoke(result: AiGenerationResult) = repository
        .insert(result)
        .ignoreElement()
}
