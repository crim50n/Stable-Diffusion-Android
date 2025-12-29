package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.FalAiGenerationDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.repository.FalAiEndpointRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import io.reactivex.rxjava3.core.Single

internal class FalAiGenerationRepositoryImpl(
    private val remoteDataSource: FalAiGenerationDataSource.Remote,
    private val endpointRepository: FalAiEndpointRepository,
) : FalAiGenerationRepository {

    override fun validateApiKey(): Single<Boolean> = remoteDataSource.validateApiKey()

    override fun generateFromText(payload: TextToImagePayload): Single<AiGenerationResult> =
        endpointRepository.getSelected()
            .map { endpoint -> endpoint.endpointId }
            .flatMap { endpointId -> remoteDataSource.textToImage(endpointId, payload) }

    override fun generateDynamic(
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
    ): Single<List<AiGenerationResult>> = remoteDataSource.generateDynamic(endpoint, parameters)
}
