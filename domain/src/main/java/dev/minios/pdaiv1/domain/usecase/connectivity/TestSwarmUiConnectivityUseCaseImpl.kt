package dev.minios.pdaiv1.domain.usecase.connectivity

import dev.minios.pdaiv1.domain.repository.SwarmUiGenerationRepository

class TestSwarmUiConnectivityUseCaseImpl(
    private val repository: SwarmUiGenerationRepository,
) : TestSwarmUiConnectivityUseCase {

    override fun invoke(url: String) = repository.checkApiAvailability(url)
}
