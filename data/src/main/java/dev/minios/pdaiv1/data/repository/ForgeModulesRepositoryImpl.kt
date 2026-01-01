package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.data.mappers.mapRawToDomain
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.entity.ForgeModule
import dev.minios.pdaiv1.domain.repository.ForgeModulesRepository
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi.Companion.PATH_SD_MODULES
import dev.minios.pdaiv1.network.model.ForgeModuleRaw
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
