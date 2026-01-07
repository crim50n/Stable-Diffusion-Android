package dev.minios.pdaiv1.domain.usecase.gallery

import io.reactivex.rxjava3.core.Completable

interface UnlikeItemsUseCase {
    operator fun invoke(ids: List<Long>): Completable
}
