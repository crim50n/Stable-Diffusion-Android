package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class DeleteAllUnlikedUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : DeleteAllUnlikedUseCase {

    override fun invoke() = generationResultRepository.deleteAllUnliked()
}
