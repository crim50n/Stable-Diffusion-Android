package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.FalAiEndpoint
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Single

interface FalAiGenerationRepository {
    fun validateApiKey(): Single<Boolean>
    fun generateFromText(payload: TextToImagePayload): Single<AiGenerationResult>
    fun generateDynamic(endpoint: FalAiEndpoint, parameters: Map<String, Any?>): Single<List<AiGenerationResult>>
}
