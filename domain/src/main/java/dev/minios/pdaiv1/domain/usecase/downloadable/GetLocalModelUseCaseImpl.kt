package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.datasource.DownloadableModelDataSource
import dev.minios.pdaiv1.domain.entity.LocalAiModel
import io.reactivex.rxjava3.core.Single

internal class GetLocalModelUseCaseImpl(
    private val localDataSource: DownloadableModelDataSource.Local,
) : GetLocalModelUseCase {

    override fun invoke(id: String): Single<LocalAiModel> = localDataSource.getById(id)
}
