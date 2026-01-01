package dev.minios.pdaiv1.domain.datasource

import dev.minios.pdaiv1.domain.entity.StabilityAiEngine
import io.reactivex.rxjava3.core.Single

sealed interface StabilityAiEnginesDataSource {
    interface Remote : StabilityAiGenerationDataSource {
        fun fetch(): Single<List<StabilityAiEngine>>
    }
}
