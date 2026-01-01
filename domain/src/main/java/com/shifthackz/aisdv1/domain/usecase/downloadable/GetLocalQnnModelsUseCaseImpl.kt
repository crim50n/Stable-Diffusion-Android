package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository

internal class GetLocalQnnModelsUseCaseImpl(
    private val downloadableModelRepository: DownloadableModelRepository,
) : GetLocalQnnModelsUseCase {

    override fun invoke() = downloadableModelRepository.getAllQnn()
}
