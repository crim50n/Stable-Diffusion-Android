package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToEmbeddingDomain
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.EmbeddingsDataSource
import dev.minios.pdaiv1.domain.entity.Embedding
import dev.minios.pdaiv1.network.api.swarmui.SwarmUiApi
import dev.minios.pdaiv1.network.api.swarmui.SwarmUiApi.Companion.PATH_MODELS
import dev.minios.pdaiv1.network.request.SwarmUiModelsRequest
import dev.minios.pdaiv1.network.response.SwarmUiModelsResponse
import io.reactivex.rxjava3.core.Single

class SwarmUiEmbeddingsRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: SwarmUiApi,
) : EmbeddingsDataSource.Remote.SwarmUi {

    override fun fetchEmbeddings(sessionId: String): Single<List<Embedding>> = serverUrlProvider(PATH_MODELS)
        .flatMap { url ->
            val request = SwarmUiModelsRequest(
                sessionId = sessionId,
                subType = "Embedding",
                path = "",
                depth = 3,
            )
            api.fetchModels(url, request)
        }
        .map(SwarmUiModelsResponse::mapRawToEmbeddingDomain)
}
