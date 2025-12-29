package com.shifthackz.aisdv1.domain.usecase.connectivity

import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository

internal class TestFalAiApiKeyUseCaseImpl(
    private val falAiGenerationRepository: FalAiGenerationRepository,
) : TestFalAiApiKeyUseCase {

    override fun invoke() = falAiGenerationRepository.validateApiKey()
}
