package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

internal class GetAllGalleryUseCaseImpl(
    private val repository: GenerationResultRepository,
) : GetAllGalleryUseCase {

    override operator fun invoke() = repository.getAll()
}
