package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.entity.MediaStoreInfo
import io.reactivex.rxjava3.core.Single

interface GetMediaStoreInfoUseCase {
    operator fun invoke(): Single<MediaStoreInfo>
}
