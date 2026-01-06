package dev.minios.pdaiv1.domain.usecase.gallery

import io.reactivex.rxjava3.core.Single

/**
 * Returns all gallery item IDs with BlurHash sorted by creation date (newest first).
 * This is a lightweight operation that only fetches IDs and BlurHash strings.
 */
interface GetGalleryPagedIdsUseCase {
    operator fun invoke(): Single<List<Long>>
    fun withBlurHash(): Single<List<Pair<Long, String>>>
}
