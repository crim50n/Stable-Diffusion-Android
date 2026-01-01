package dev.minios.pdaiv1.domain.usecase.stabilityai

import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.entity.StabilityAiEngine
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.StabilityAiEnginesRepository
import io.reactivex.rxjava3.core.Single

internal class FetchAndGetStabilityAiEnginesUseCaseImpl(
    private val repository: StabilityAiEnginesRepository,
    private val preferenceManager: PreferenceManager,
) : FetchAndGetStabilityAiEnginesUseCase {

    override fun invoke(): Single<List<StabilityAiEngine>> {
        if (preferenceManager.source != ServerSource.STABILITY_AI) {
            return Single.just(emptyList())
        }
        return repository
            .fetchAndGet()
            .flatMap { engines ->
                if (!engines.map(StabilityAiEngine::id).contains(preferenceManager.stabilityAiEngineId)) {
                    preferenceManager.stabilityAiEngineId = engines.firstOrNull()?.id ?: ""
                }
                Single.just(engines)
            }
    }
}
