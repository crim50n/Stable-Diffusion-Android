package dev.minios.pdaiv1.domain.usecase.forgemodule

import dev.minios.pdaiv1.domain.entity.ForgeModule
import io.reactivex.rxjava3.core.Single

interface GetForgeModulesUseCase {
    operator fun invoke(): Single<List<ForgeModule>>
}
