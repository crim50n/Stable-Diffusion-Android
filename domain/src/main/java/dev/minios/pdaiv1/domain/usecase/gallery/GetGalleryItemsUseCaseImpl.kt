package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class GetGalleryItemsUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : GetGalleryItemsUseCase {

    override fun invoke(ids: List<Long>) = generationResultRepository.getByIds(ids)
}
