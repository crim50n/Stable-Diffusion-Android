package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Single

interface StabilityAiGenerationRepository {
    fun validateApiKey(): Single<Boolean>
    fun generateFromText(payload: TextToImagePayload): Single<AiGenerationResult>
    fun generateFromImage(payload: ImageToImagePayload): Single<AiGenerationResult>
}
