package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.entity.ThumbnailData
import io.reactivex.rxjava3.core.Single

interface GetThumbnailInfoUseCase {
    operator fun invoke(ids: List<Long>): Single<List<ThumbnailData>>
}
