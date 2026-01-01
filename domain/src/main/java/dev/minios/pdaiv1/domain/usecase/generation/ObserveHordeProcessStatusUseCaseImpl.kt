package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.repository.HordeGenerationRepository

internal class ObserveHordeProcessStatusUseCaseImpl(
    private val hordeGenerationRepository: HordeGenerationRepository,
) : ObserveHordeProcessStatusUseCase {

    override fun invoke() = hordeGenerationRepository
        .observeStatus()
        .distinctUntilChanged()
}
