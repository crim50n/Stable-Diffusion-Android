package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class GetGalleryItemsRawUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : GetGalleryItemsRawUseCase {

    override fun invoke(ids: List<Long>) = generationResultRepository.getByIdsRaw(ids)
}
