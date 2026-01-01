package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.domain.datasource.SupportersDataSource
import dev.minios.pdaiv1.domain.entity.Supporter
import dev.minios.pdaiv1.domain.repository.SupportersRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class SupportersRepositoryImpl(
    private val rds: SupportersDataSource.Remote,
    private val lds: SupportersDataSource.Local,
) : SupportersRepository {

    override fun fetchSupporters(): Completable = rds
        .fetch()
        .flatMapCompletable(lds::save)

    override fun fetchAndGetSupporters(): Single<List<Supporter>> = fetchSupporters()
        .onErrorComplete()
        .andThen(getSupporters())

    override fun getSupporters(): Single<List<Supporter>> = lds.getAll()
}
