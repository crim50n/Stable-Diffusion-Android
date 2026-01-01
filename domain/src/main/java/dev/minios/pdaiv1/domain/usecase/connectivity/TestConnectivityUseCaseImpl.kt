package dev.minios.pdaiv1.domain.usecase.connectivity

import dev.minios.pdaiv1.domain.repository.StableDiffusionGenerationRepository

internal class TestConnectivityUseCaseImpl(
    private val repository: StableDiffusionGenerationRepository,
) : TestConnectivityUseCase {

    override fun invoke(url: String) = repository.checkApiAvailability(url)
}
