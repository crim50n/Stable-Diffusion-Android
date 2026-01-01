package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.Supporter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface SupportersRepository {
    fun fetchSupporters(): Completable
    fun fetchAndGetSupporters(): Single<List<Supporter>>
    fun getSupporters(): Single<List<Supporter>>
}
