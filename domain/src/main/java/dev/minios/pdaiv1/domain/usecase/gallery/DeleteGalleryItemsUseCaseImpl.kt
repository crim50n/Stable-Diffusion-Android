package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Completable

internal class DeleteGalleryItemsUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : DeleteGalleryItemsUseCase {

    override fun invoke(ids: List<Long>): Completable = generationResultRepository.deleteByIdList(ids)
}
