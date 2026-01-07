package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class UnhideItemsUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : UnhideItemsUseCase {

    override fun invoke(ids: List<Long>) = generationResultRepository.unhideByIds(ids)
}
