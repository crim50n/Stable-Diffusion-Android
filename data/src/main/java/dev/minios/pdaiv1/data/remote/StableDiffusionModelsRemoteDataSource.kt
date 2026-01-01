package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToCheckpointDomain
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.StableDiffusionModelsDataSource
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi.Companion.PATH_SD_MODELS
import dev.minios.pdaiv1.network.model.StableDiffusionModelRaw

internal class StableDiffusionModelsRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: Automatic1111RestApi,
) : StableDiffusionModelsDataSource.Remote {

    override fun fetchSdModels() = serverUrlProvider(PATH_SD_MODELS)
        .flatMap(api::fetchSdModels)
        .map(List<StableDiffusionModelRaw>::mapRawToCheckpointDomain)
}
