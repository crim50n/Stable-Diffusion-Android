package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Single

sealed interface FalAiGenerationDataSource {

    interface Remote : FalAiGenerationDataSource {

        fun validateApiKey(): Single<Boolean>

        fun textToImage(model: String, payload: TextToImagePayload): Single<AiGenerationResult>

        fun generateDynamic(
            endpoint: FalAiEndpoint,
            parameters: Map<String, Any?>,
        ): Single<List<AiGenerationResult>>
    }
}
