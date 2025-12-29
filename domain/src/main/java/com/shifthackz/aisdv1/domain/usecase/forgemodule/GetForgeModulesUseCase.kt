package com.shifthackz.aisdv1.domain.usecase.forgemodule

import com.shifthackz.aisdv1.domain.entity.ForgeModule
import io.reactivex.rxjava3.core.Single

interface GetForgeModulesUseCase {
    operator fun invoke(): Single<List<ForgeModule>>
}
