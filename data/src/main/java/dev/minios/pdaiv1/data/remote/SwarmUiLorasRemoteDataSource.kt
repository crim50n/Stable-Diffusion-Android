package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToLoraDomain
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.LorasDataSource
import dev.minios.pdaiv1.domain.entity.LoRA
import dev.minios.pdaiv1.network.api.swarmui.SwarmUiApi
import dev.minios.pdaiv1.network.api.swarmui.SwarmUiApi.Companion.PATH_MODELS
import dev.minios.pdaiv1.network.request.SwarmUiModelsRequest
import dev.minios.pdaiv1.network.response.SwarmUiModelsResponse
import io.reactivex.rxjava3.core.Single

internal class SwarmUiLorasRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: SwarmUiApi,
) : LorasDataSource.Remote.SwarmUi {

    override fun fetchLoras(sessionId: String): Single<List<LoRA>> = serverUrlProvider(PATH_MODELS)
        .flatMap { url ->
            val request = SwarmUiModelsRequest(
                sessionId = sessionId,
                subType = "LoRA",
                path = "",
                depth = 3,
            )
            api.fetchModels(url, request)
        }
        .map(SwarmUiModelsResponse::mapRawToLoraDomain)
}
