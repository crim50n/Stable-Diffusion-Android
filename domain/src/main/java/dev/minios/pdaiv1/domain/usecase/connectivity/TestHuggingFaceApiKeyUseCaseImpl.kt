package dev.minios.pdaiv1.domain.usecase.connectivity

import dev.minios.pdaiv1.domain.repository.HuggingFaceGenerationRepository

internal class TestHuggingFaceApiKeyUseCaseImpl(
    private val huggingFaceGenerationRepository: HuggingFaceGenerationRepository,
) : TestHuggingFaceApiKeyUseCase {

    override fun invoke() = huggingFaceGenerationRepository.validateApiKey()
}
