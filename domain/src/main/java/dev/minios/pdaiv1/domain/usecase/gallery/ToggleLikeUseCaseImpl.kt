package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class ToggleLikeUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : ToggleLikeUseCase {

    override fun invoke(id: Long) = generationResultRepository.toggleLike(id)
}
