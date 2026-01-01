package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapToDomain
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.LorasDataSource
import dev.minios.pdaiv1.domain.entity.LoRA
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi.Companion.PATH_LORAS
import dev.minios.pdaiv1.network.model.StableDiffusionLoraRaw
import io.reactivex.rxjava3.core.Single

internal class StableDiffusionLorasRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: Automatic1111RestApi,
) : LorasDataSource.Remote.Automatic1111 {

    override fun fetchLoras(): Single<List<LoRA>> = serverUrlProvider(PATH_LORAS)
        .flatMap(api::fetchLoras)
        .map(List<StableDiffusionLoraRaw>::mapToDomain)
}
