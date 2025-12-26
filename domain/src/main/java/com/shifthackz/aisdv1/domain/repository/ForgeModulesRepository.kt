package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.ForgeModule
import io.reactivex.rxjava3.core.Single

interface ForgeModulesRepository {
    fun fetchModules(): Single<List<ForgeModule>>
}
