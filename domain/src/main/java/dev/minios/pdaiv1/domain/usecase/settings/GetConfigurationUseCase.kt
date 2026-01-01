package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.entity.Configuration
import io.reactivex.rxjava3.core.Single

interface GetConfigurationUseCase {
    operator fun invoke(): Single<Configuration>
}
