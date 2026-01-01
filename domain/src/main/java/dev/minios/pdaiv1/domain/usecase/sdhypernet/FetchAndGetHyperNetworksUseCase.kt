package dev.minios.pdaiv1.domain.usecase.sdhypernet

import dev.minios.pdaiv1.domain.entity.StableDiffusionHyperNetwork
import io.reactivex.rxjava3.core.Single

interface FetchAndGetHyperNetworksUseCase {
    operator fun invoke(): Single<List<StableDiffusionHyperNetwork>>
}
