package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.StabilityAiEngine
import io.reactivex.rxjava3.core.Single

interface StabilityAiEnginesRepository {
    fun fetchAndGet(): Single<List<StabilityAiEngine>>
}
