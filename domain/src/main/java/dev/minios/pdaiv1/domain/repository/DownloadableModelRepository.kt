package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.DownloadState
import dev.minios.pdaiv1.domain.entity.LocalAiModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface DownloadableModelRepository {
    fun download(id: String, url: String): Observable<DownloadState>
    fun delete(id: String): Completable
    fun getAllOnnx(): Single<List<LocalAiModel>>
    fun getAllMediaPipe(): Single<List<LocalAiModel>>
    fun getAllQnn(): Single<List<LocalAiModel>>
    fun observeAllOnnx(): Flowable<List<LocalAiModel>>
}
