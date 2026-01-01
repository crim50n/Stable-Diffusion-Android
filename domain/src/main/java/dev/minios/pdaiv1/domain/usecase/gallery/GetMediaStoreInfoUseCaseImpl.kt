package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository

class GetMediaStoreInfoUseCaseImpl(
    private val repository: GenerationResultRepository,
) : GetMediaStoreInfoUseCase {

    override fun invoke() = repository.getMediaStoreInfo()
}
