package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class GetThumbnailInfoUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : GetThumbnailInfoUseCase {

    override fun invoke(ids: List<Long>) = generationResultRepository.getThumbnailInfoByIds(ids)
}
