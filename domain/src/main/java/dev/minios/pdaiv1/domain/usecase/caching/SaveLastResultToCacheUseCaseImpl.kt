package dev.minios.pdaiv1.domain.usecase.caching

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.TemporaryGenerationResultRepository
import io.reactivex.rxjava3.core.Single

internal class SaveLastResultToCacheUseCaseImpl(
    private val temporaryGenerationResultRepository: TemporaryGenerationResultRepository,
    private val preferenceManager: PreferenceManager,
) : SaveLastResultToCacheUseCase {

    override fun invoke(result: AiGenerationResult): Single<AiGenerationResult> {
        if (preferenceManager.autoSaveAiResults) return Single.just(result)
        return temporaryGenerationResultRepository
            .put(result)
            .andThen(Single.just(result))
    }
}
