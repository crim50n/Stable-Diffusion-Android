package dev.minios.pdaiv1.domain.usecase.settings

import io.reactivex.rxjava3.core.Single

interface ConnectToLocalDiffusionUseCase {
    operator fun invoke(modelId: String): Single<Result<Unit>>
}
