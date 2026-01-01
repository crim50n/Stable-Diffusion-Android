package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.domain.datasource.StableDiffusionModelsDataSource
import dev.minios.pdaiv1.domain.entity.StableDiffusionModel
import dev.minios.pdaiv1.domain.repository.StableDiffusionModelsRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class StableDiffusionModelsRepositoryImpl(
    private val remoteDataSource: StableDiffusionModelsDataSource.Remote,
    private val localDataSource: StableDiffusionModelsDataSource.Local,
) : StableDiffusionModelsRepository {

    override fun fetchModels(): Completable = remoteDataSource
        .fetchSdModels()
        .flatMapCompletable(localDataSource::insertModels)

    override fun fetchAndGetModels(): Single<List<StableDiffusionModel>> = fetchModels()
        .onErrorComplete()
        .andThen(getModels())

    override fun getModels(): Single<List<StableDiffusionModel>> =
        localDataSource.getModels()
}
