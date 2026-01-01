package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Completable

internal class DeleteAllGalleryUseCaseImpl(
    private val generationResultRepository: GenerationResultRepository,
) : DeleteAllGalleryUseCase {

    override fun invoke(): Completable = generationResultRepository.deleteAll()
}
