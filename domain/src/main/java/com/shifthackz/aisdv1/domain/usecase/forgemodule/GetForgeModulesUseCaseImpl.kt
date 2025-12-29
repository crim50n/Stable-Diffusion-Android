package com.shifthackz.aisdv1.domain.usecase.forgemodule

import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.repository.ForgeModulesRepository
import io.reactivex.rxjava3.core.Single

internal class GetForgeModulesUseCaseImpl(
    private val repository: ForgeModulesRepository,
) : GetForgeModulesUseCase {

    override operator fun invoke(): Single<List<ForgeModule>> = repository.fetchModules()
}
