package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mappers.mapRawToCheckpointDomain
import dev.minios.pdaiv1.domain.datasource.StabilityAiEnginesDataSource
import dev.minios.pdaiv1.domain.entity.StabilityAiEngine
import dev.minios.pdaiv1.network.api.stabilityai.StabilityAiApi
import dev.minios.pdaiv1.network.model.StabilityAiEngineRaw
import io.reactivex.rxjava3.core.Single

internal class StabilityAiEnginesRemoteDataSource(
    private val api: StabilityAiApi,
) : StabilityAiEnginesDataSource.Remote {

    override fun fetch(): Single<List<StabilityAiEngine>> = api
        .fetchEngines()
        .map(List<StabilityAiEngineRaw>::mapRawToCheckpointDomain)
}
