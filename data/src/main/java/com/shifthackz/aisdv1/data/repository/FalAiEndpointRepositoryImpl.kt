package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.mappers.FalAiOpenApiParser
import com.shifthackz.aisdv1.domain.datasource.FalAiEndpointDataSource
import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.FalAiEndpointRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal class FalAiEndpointRepositoryImpl(
    private val builtInDataSource: FalAiEndpointDataSource.BuiltIn,
    private val remoteDataSource: FalAiEndpointDataSource.Remote,
    private val localDataSource: FalAiEndpointDataSource.Local,
    private val preferenceManager: PreferenceManager,
) : FalAiEndpointRepository {

    override fun observeAll(): Observable<List<FalAiEndpoint>> = Observable
        .combineLatest(
            builtInDataSource.getAll().toObservable(),
            localDataSource.observeAll().startWithItem(emptyList()),
        ) { builtIn, custom ->
            builtIn + custom
        }

    override fun getAll(): Single<List<FalAiEndpoint>> = Single
        .zip(
            builtInDataSource.getAll(),
            localDataSource.getAll().onErrorReturnItem(emptyList()),
        ) { builtIn, custom ->
            builtIn + custom
        }

    override fun getById(id: String): Single<FalAiEndpoint> = getAll()
        .flatMap { endpoints ->
            endpoints.find { it.endpointId == id || it.id == id }
                ?.let { Single.just(it) }
                ?: Single.error(NoSuchElementException("Endpoint not found: $id"))
        }

    override fun getSelected(): Single<FalAiEndpoint> {
        val selectedId = preferenceManager.falAiSelectedEndpointId
        return if (selectedId.isBlank()) {
            // Return default endpoint (first built-in)
            builtInDataSource.getAll().map { it.first() }
        } else {
            getById(selectedId).onErrorResumeNext {
                // Fallback to first built-in if selected not found
                builtInDataSource.getAll().map { it.first() }
            }
        }
    }

    override fun setSelected(id: String): Completable = Completable.fromAction {
        preferenceManager.falAiSelectedEndpointId = id
    }

    override fun importFromJson(json: String): Single<FalAiEndpoint> = Single
        .fromCallable { FalAiOpenApiParser.parse(json, isCustom = true) }
        .flatMap { endpoint ->
            localDataSource.save(endpoint).andThen(Single.just(endpoint))
        }

    override fun importFromUrl(url: String): Single<FalAiEndpoint> = remoteDataSource
        .fetchFromUrl(url)
        .flatMap { endpoint ->
            localDataSource.save(endpoint).andThen(Single.just(endpoint))
        }

    override fun delete(id: String): Completable = localDataSource.delete(id)
}
