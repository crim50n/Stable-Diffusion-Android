package dev.minios.pdaiv1.domain.usecase.settings

import io.reactivex.rxjava3.core.Single

interface ConnectToFalAiUseCase {
    operator fun invoke(apiKey: String, endpointId: String): Single<Result<Unit>>
}
