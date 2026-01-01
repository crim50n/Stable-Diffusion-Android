package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.repository.DownloadableModelRepository

internal class GetLocalMediaPipeModelsUseCaseImpl(
    private val downloadableModelRepository: DownloadableModelRepository,
    ) : GetLocalMediaPipeModelsUseCase {

    override fun invoke() = downloadableModelRepository.getAllMediaPipe()
}
