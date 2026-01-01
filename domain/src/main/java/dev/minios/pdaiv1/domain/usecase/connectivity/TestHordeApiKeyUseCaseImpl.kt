package dev.minios.pdaiv1.domain.usecase.connectivity

import dev.minios.pdaiv1.domain.repository.HordeGenerationRepository

internal class TestHordeApiKeyUseCaseImpl(
    private val hordeGenerationRepository: HordeGenerationRepository,
) : TestHordeApiKeyUseCase {

    override fun invoke() = hordeGenerationRepository.validateApiKey()
}
