package dev.minios.pdaiv1.domain.usecase.connectivity

import dev.minios.pdaiv1.domain.repository.StabilityAiGenerationRepository

internal class TestStabilityAiApiKeyUseCaseImpl(
    private val stabilityAiGenerationRepository: StabilityAiGenerationRepository,
) : TestStabilityAiApiKeyUseCase {

    override fun invoke() = stabilityAiGenerationRepository.validateApiKey()
}
