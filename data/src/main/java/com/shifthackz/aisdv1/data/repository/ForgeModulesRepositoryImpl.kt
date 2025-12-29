package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.mappers.mapRawToDomain
import com.shifthackz.aisdv1.data.provider.ServerUrlProvider
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.repository.ForgeModulesRepository
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111RestApi
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111RestApi.Companion.PATH_SD_MODULES
import com.shifthackz.aisdv1.network.model.ForgeModuleRaw
import io.reactivex.rxjava3.core.Single

internal class ForgeModulesRepositoryImpl(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: Automatic1111RestApi,
) : ForgeModulesRepository {

    override fun fetchModules(): Single<List<ForgeModule>> = serverUrlProvider(PATH_SD_MODULES)
        .flatMap(api::fetchForgeModules)
        .map(List<ForgeModuleRaw>::mapRawToDomain)
        .onErrorReturn { emptyList() }
}
