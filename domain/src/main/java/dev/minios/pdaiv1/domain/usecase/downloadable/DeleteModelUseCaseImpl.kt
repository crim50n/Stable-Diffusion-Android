package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.repository.DownloadableModelRepository

internal class DeleteModelUseCaseImpl(
    private val downloadableModelRepository: DownloadableModelRepository,
) : DeleteModelUseCase {

    override fun invoke(id: String) = downloadableModelRepository.delete(id)
}
