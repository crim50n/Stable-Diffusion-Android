package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.repository.DownloadableModelRepository

internal class GetLocalQnnModelsUseCaseImpl(
    private val downloadableModelRepository: DownloadableModelRepository,
) : GetLocalQnnModelsUseCase {

    override fun invoke() = downloadableModelRepository.getAllQnn()
}
