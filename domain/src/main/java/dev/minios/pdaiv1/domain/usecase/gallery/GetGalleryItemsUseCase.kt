package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import io.reactivex.rxjava3.core.Single

interface GetGalleryItemsUseCase {
    operator fun invoke(ids: List<Long>): Single<List<AiGenerationResult>>
}
