package dev.minios.pdaiv1.domain.usecase.sdhypernet

import dev.minios.pdaiv1.domain.repository.StableDiffusionHyperNetworksRepository

internal class FetchAndGetHyperNetworksUseCaseImpl(
    private val stableDiffusionHyperNetworksRepository: StableDiffusionHyperNetworksRepository,
) : FetchAndGetHyperNetworksUseCase {
    override fun invoke() = stableDiffusionHyperNetworksRepository.fetchAndGetHyperNetworks()
}
