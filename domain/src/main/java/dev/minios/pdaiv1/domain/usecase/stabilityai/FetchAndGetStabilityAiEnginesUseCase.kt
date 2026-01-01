package dev.minios.pdaiv1.domain.usecase.stabilityai

import dev.minios.pdaiv1.domain.entity.StabilityAiEngine
import io.reactivex.rxjava3.core.Single

interface FetchAndGetStabilityAiEnginesUseCase {
    operator fun invoke(): Single<List<StabilityAiEngine>>
}
