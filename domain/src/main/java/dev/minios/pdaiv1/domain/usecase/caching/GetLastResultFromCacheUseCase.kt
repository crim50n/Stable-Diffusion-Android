package dev.minios.pdaiv1.domain.usecase.caching

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import io.reactivex.rxjava3.core.Single

interface GetLastResultFromCacheUseCase {
    operator fun invoke(): Single<AiGenerationResult>
}
