package dev.minios.pdaiv1.domain.usecase.connectivity

import dev.minios.pdaiv1.domain.repository.FalAiGenerationRepository

internal class TestFalAiApiKeyUseCaseImpl(
    private val falAiGenerationRepository: FalAiGenerationRepository,
) : TestFalAiApiKeyUseCase {

    override fun invoke() = falAiGenerationRepository.validateApiKey()
}
