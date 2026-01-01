package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToCheckpointDomain
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.StableDiffusionSamplersDataSource
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi.Companion.PATH_SAMPLERS
import dev.minios.pdaiv1.network.model.StableDiffusionSamplerRaw

internal class StableDiffusionSamplersRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: Automatic1111RestApi,
) : StableDiffusionSamplersDataSource.Remote {

    override fun fetchSamplers() = serverUrlProvider(PATH_SAMPLERS)
        .flatMap(api::fetchSamplers)
        .map(List<StableDiffusionSamplerRaw>::mapRawToCheckpointDomain)
}