package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.domain.datasource.StabilityAiEnginesDataSource
import dev.minios.pdaiv1.domain.repository.StabilityAiEnginesRepository

internal class StabilityAiEnginesRepositoryImpl(
    private val remoteDataSource: StabilityAiEnginesDataSource.Remote,
) : StabilityAiEnginesRepository {

    override fun fetchAndGet() = remoteDataSource.fetch()
}
