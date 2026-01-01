package dev.minios.pdaiv1.domain.usecase.settings

import io.reactivex.rxjava3.core.Single

interface ConnectToQnnUseCase {
    operator fun invoke(modelId: String, runOnCpu: Boolean = false): Single<Result<Unit>>
}
