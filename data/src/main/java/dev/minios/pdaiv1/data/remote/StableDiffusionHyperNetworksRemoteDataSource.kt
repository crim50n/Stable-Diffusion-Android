package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToCheckpointDomain
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.StableDiffusionHyperNetworksDataSource
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi.Companion.PATH_HYPER_NETWORKS
import dev.minios.pdaiv1.network.model.StableDiffusionHyperNetworkRaw

internal class StableDiffusionHyperNetworksRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: Automatic1111RestApi,
) : StableDiffusionHyperNetworksDataSource.Remote {

    override fun fetchHyperNetworks() = serverUrlProvider(PATH_HYPER_NETWORKS)
        .flatMap(api::fetchHyperNetworks)
        .map(List<StableDiffusionHyperNetworkRaw>::mapRawToCheckpointDomain)
}
