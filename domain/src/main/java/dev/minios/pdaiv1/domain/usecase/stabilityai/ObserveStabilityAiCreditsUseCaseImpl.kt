package dev.minios.pdaiv1.domain.usecase.stabilityai

import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.entity.Settings
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.StabilityAiCreditsRepository
import io.reactivex.rxjava3.core.Flowable

internal class ObserveStabilityAiCreditsUseCaseImpl(
    private val repository: StabilityAiCreditsRepository,
    private val preferenceManager: PreferenceManager,
) : ObserveStabilityAiCreditsUseCase {

    override fun invoke() = Flowable
        .combineLatest(
            preferenceManager.observe().map(Settings::source),
            repository.fetchAndObserve(),
            ::Pair,
        )
        .map(Pair<ServerSource, Float>::second)
        .onErrorReturn { 0f }
}
