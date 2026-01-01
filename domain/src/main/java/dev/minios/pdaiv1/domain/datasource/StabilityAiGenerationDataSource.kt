package dev.minios.pdaiv1.domain.datasource

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Single

sealed interface StabilityAiGenerationDataSource {

    interface Remote : StabilityAiGenerationDataSource {

        fun validateApiKey(): Single<Boolean>

        fun textToImage(engineId: String, payload: TextToImagePayload): Single<AiGenerationResult>

        fun imageToImage(
            engineId: String,
            payload: ImageToImagePayload,
            imageBytes: ByteArray,
        ): Single<AiGenerationResult>
    }
}
