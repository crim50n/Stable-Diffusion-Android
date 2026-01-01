package dev.minios.pdaiv1.domain.usecase.connectivity

import dev.minios.pdaiv1.domain.repository.OpenAiGenerationRepository

internal class TestOpenAiApiKeyUseCaseImpl(
    private val openAiGenerationRepository: OpenAiGenerationRepository,
) : TestOpenAiApiKeyUseCase {

    override fun invoke() = openAiGenerationRepository.validateApiKey()
}
