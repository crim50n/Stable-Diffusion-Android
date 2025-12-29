package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface FalAiEndpointRepository {

    /**
     * Get all available endpoints (built-in + custom).
     */
    fun observeAll(): Observable<List<FalAiEndpoint>>

    /**
     * Get all endpoints once.
     */
    fun getAll(): Single<List<FalAiEndpoint>>

    /**
     * Get endpoint by ID.
     */
    fun getById(id: String): Single<FalAiEndpoint>

    /**
     * Get currently selected endpoint.
     */
    fun getSelected(): Single<FalAiEndpoint>

    /**
     * Set selected endpoint ID.
     */
    fun setSelected(id: String): Completable

    /**
     * Import custom endpoint from OpenAPI JSON string.
     */
    fun importFromJson(json: String): Single<FalAiEndpoint>

    /**
     * Import custom endpoint from URL.
     */
    fun importFromUrl(url: String): Single<FalAiEndpoint>

    /**
     * Delete custom endpoint.
     */
    fun delete(id: String): Completable
}
