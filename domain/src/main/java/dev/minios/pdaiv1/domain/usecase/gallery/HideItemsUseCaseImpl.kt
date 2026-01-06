package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class HideItemsUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : HideItemsUseCase {

    override fun invoke(ids: List<Long>) = generationResultRepository.hideByIds(ids)
}
