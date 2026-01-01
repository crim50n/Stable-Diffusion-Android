package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.ForgeModule
import io.reactivex.rxjava3.core.Single

interface ForgeModulesRepository {
    fun fetchModules(): Single<List<ForgeModule>>
}
