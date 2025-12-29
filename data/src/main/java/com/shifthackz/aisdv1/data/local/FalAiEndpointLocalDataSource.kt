package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.data.mappers.toDomain
import com.shifthackz.aisdv1.data.mappers.toEntity
import com.shifthackz.aisdv1.domain.datasource.FalAiEndpointDataSource
import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import com.shifthackz.aisdv1.storage.db.persistent.dao.FalAiEndpointDao
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Local storage for custom fal.ai endpoints.
 */
internal class FalAiEndpointLocalDataSource(
    private val dao: FalAiEndpointDao,
) : FalAiEndpointDataSource.Local {

    override fun observeAll(): Observable<List<FalAiEndpoint>> = dao
        .observeAll()
        .map { entities -> entities.map { it.toDomain() } }
        .toObservable()

    override fun getAll(): Single<List<FalAiEndpoint>> = dao
        .queryAll()
        .map { entities -> entities.map { it.toDomain() } }

    override fun getById(id: String): Single<FalAiEndpoint> = dao
        .queryById(id)
        .map { it.toDomain() }

    override fun save(endpoint: FalAiEndpoint): Completable = dao
        .insert(endpoint.toEntity())

    override fun delete(id: String): Completable = dao
        .deleteById(id)
}
