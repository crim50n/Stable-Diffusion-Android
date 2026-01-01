package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.feature.auth.AuthorizationCredentials
import io.reactivex.rxjava3.core.Single

interface ConnectToA1111UseCase {
    operator fun invoke(
        url: String,
        isDemo: Boolean,
        credentials: AuthorizationCredentials,
    ): Single<Result<Unit>>
}
