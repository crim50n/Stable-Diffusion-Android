package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.domain.datasource.StableDiffusionSamplersDataSource
import dev.minios.pdaiv1.domain.repository.StableDiffusionSamplersRepository

internal class StableDiffusionSamplersRepositoryImpl(
    private val remoteDataSource: StableDiffusionSamplersDataSource.Remote,
    private val localDataSource: StableDiffusionSamplersDataSource.Local,
) : StableDiffusionSamplersRepository {

    override fun fetchSamplers() = remoteDataSource
        .fetchSamplers()
        .flatMapCompletable(localDataSource::insertSamplers)

    override fun getSamplers() = localDataSource.getSamplers()
}
