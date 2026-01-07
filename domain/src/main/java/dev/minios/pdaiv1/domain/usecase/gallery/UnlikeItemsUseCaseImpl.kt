package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class UnlikeItemsUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : UnlikeItemsUseCase {

    override fun invoke(ids: List<Long>) = generationResultRepository.unlikeByIds(ids)
}
