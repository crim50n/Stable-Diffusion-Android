package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.feature.auth.AuthorizationCredentials
import io.reactivex.rxjava3.core.Single

interface ConnectToSwarmUiUseCase {
    operator fun invoke(url: String, credentials: AuthorizationCredentials): Single<Result<Unit>>
}
