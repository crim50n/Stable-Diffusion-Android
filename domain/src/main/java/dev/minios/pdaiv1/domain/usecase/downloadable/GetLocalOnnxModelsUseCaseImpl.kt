package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.repository.DownloadableModelRepository

internal class GetLocalOnnxModelsUseCaseImpl(
    private val downloadableModelRepository: DownloadableModelRepository,
) : GetLocalOnnxModelsUseCase {

    override fun invoke() = downloadableModelRepository.getAllOnnx()
}
