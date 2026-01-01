package dev.minios.pdaiv1.domain.usecase.gallery

import io.reactivex.rxjava3.core.Single

/**
 * Returns all gallery item IDs sorted by creation date (newest first).
 * This is a lightweight operation that only fetches IDs, not full items.
 */
interface GetGalleryPagedIdsUseCase {
    operator fun invoke(): Single<List<Long>>
}
