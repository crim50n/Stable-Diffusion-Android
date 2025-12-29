package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface FalAiEndpointDataSource {

    /**
     * Provides built-in endpoints shipped with the app.
     */
    interface BuiltIn {
        fun getAll(): Single<List<FalAiEndpoint>>
    }

    /**
     * Provides remote endpoint fetching from URL.
     */
    interface Remote {
        fun fetchFromUrl(url: String): Single<FalAiEndpoint>
    }

    /**
     * Provides local storage for custom endpoints.
     */
    interface Local {
        fun observeAll(): Observable<List<FalAiEndpoint>>
        fun getAll(): Single<List<FalAiEndpoint>>
        fun getById(id: String): Single<FalAiEndpoint>
        fun save(endpoint: FalAiEndpoint): Completable
        fun delete(id: String): Completable
    }
}
