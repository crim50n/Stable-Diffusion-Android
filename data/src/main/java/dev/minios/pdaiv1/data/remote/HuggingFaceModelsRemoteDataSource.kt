package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToCheckpointDomain
import dev.minios.pdaiv1.domain.datasource.HuggingFaceModelsDataSource
import dev.minios.pdaiv1.domain.entity.HuggingFaceModel
import dev.minios.pdaiv1.network.api.pdai.HuggingFaceModelsApi
import dev.minios.pdaiv1.network.model.HuggingFaceModelRaw

internal class HuggingFaceModelsRemoteDataSource(
    private val api: HuggingFaceModelsApi,
) : HuggingFaceModelsDataSource.Remote {

    override fun fetchHuggingFaceModels() = api
        .fetchHuggingFaceModels()
        .map(List<HuggingFaceModelRaw>::mapRawToCheckpointDomain)
        .onErrorReturn { listOf(HuggingFaceModel.default) }
}
