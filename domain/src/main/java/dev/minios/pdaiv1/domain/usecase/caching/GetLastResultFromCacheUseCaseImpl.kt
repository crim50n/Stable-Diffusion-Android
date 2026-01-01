package dev.minios.pdaiv1.domain.usecase.caching

import dev.minios.pdaiv1.domain.repository.TemporaryGenerationResultRepository

internal class GetLastResultFromCacheUseCaseImpl(
    private val temporaryGenerationResultRepository: TemporaryGenerationResultRepository,
) : GetLastResultFromCacheUseCase {

    override fun invoke() = temporaryGenerationResultRepository.get()
}
