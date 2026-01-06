package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class LikeItemsUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : LikeItemsUseCase {

    override fun invoke(ids: List<Long>) = generationResultRepository.likeByIds(ids)
}
