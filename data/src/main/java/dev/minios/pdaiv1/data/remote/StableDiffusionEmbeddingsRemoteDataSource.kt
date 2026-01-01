package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToCheckpointDomain
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.EmbeddingsDataSource
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi.Companion.PATH_EMBEDDINGS
import dev.minios.pdaiv1.network.response.SdEmbeddingsResponse

internal class StableDiffusionEmbeddingsRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: Automatic1111RestApi,
) : EmbeddingsDataSource.Remote.Automatic1111 {

    override fun fetchEmbeddings() = serverUrlProvider(PATH_EMBEDDINGS)
        .flatMap(api::fetchEmbeddings)
        .map(SdEmbeddingsResponse::mapRawToCheckpointDomain)
}
