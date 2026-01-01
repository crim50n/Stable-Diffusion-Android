package dev.minios.pdaiv1.domain.usecase.swarmmodel

import dev.minios.pdaiv1.domain.entity.SwarmUiModel
import io.reactivex.rxjava3.core.Single

interface FetchAndGetSwarmUiModelsUseCase {
    operator fun invoke(): Single<List<SwarmUiModel>>
}
