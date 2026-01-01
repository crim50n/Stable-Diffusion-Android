package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.domain.datasource.RandomImageDataSource
import dev.minios.pdaiv1.domain.repository.RandomImageRepository

internal class RandomImageRepositoryImpl(
    private val remoteDataSource: RandomImageDataSource.Remote,
) : RandomImageRepository {

    override fun fetchAndGet() = remoteDataSource.fetch()
}
