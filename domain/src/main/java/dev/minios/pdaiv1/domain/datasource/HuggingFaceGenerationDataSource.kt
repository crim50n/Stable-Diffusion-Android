package dev.minios.pdaiv1.domain.datasource

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Single

sealed interface HuggingFaceGenerationDataSource {
    interface Remote : HuggingFaceGenerationDataSource {
        fun validateApiKey(): Single<Boolean>
        fun textToImage(
            modelName: String,
            payload: TextToImagePayload,
        ): Single<AiGenerationResult>

        fun imageToImage(
            modelName: String,
            payload: ImageToImagePayload,
        ): Single<AiGenerationResult>
    }
}
