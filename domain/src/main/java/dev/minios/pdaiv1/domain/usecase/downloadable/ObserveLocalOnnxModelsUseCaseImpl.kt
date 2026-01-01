package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.repository.DownloadableModelRepository

internal class ObserveLocalOnnxModelsUseCaseImpl(
    private val repository: DownloadableModelRepository,
) : ObserveLocalOnnxModelsUseCase {

    override fun invoke() = repository
        .observeAllOnnx()
        .distinctUntilChanged()
}
