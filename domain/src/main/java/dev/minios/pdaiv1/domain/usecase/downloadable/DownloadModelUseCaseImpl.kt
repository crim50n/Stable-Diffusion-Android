package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.repository.DownloadableModelRepository

internal class DownloadModelUseCaseImpl(
    private val downloadableModelRepository: DownloadableModelRepository,
) : DownloadModelUseCase {

    override fun invoke(id: String, url: String) = downloadableModelRepository.download(id, url)
}
