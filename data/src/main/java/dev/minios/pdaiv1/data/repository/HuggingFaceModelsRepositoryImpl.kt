package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.domain.datasource.HuggingFaceModelsDataSource
import dev.minios.pdaiv1.domain.repository.HuggingFaceModelsRepository

internal class HuggingFaceModelsRepositoryImpl(
    private val remoteDataSource: HuggingFaceModelsDataSource.Remote,
    private val localDataSource: HuggingFaceModelsDataSource.Local,
) : HuggingFaceModelsRepository {

    override fun fetchHuggingFaceModels() = remoteDataSource
        .fetchHuggingFaceModels()
        .concatMapCompletable(localDataSource::save)

    override fun fetchAndGetHuggingFaceModels() = fetchHuggingFaceModels()
        .onErrorComplete()
        .andThen(getHuggingFaceModels())

    override fun getHuggingFaceModels() = localDataSource.getAll()
}
