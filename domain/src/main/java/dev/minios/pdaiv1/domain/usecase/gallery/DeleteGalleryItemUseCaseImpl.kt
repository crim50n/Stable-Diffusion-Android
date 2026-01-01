package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Completable

internal class DeleteGalleryItemUseCaseImpl(
    private val repository: GenerationResultRepository,
) : DeleteGalleryItemUseCase {

    override fun invoke(id: Long): Completable = repository.deleteById(id)
}
