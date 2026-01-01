package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.entity.DownloadState
import io.reactivex.rxjava3.core.Observable

interface DownloadModelUseCase {
    operator fun invoke(id: String, url: String): Observable<DownloadState>
}
