package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import io.reactivex.rxjava3.core.Single

/**
 * Returns gallery items WITHOUT loading full images from files.
 * Use this for thumbnail loading where only mediaPath is needed.
 */
interface GetGalleryItemsRawUseCase {
    operator fun invoke(ids: List<Long>): Single<List<AiGenerationResult>>
}
