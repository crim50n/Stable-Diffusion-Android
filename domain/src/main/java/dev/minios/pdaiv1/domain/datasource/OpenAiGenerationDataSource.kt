package dev.minios.pdaiv1.domain.datasource

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Single

sealed interface OpenAiGenerationDataSource {
    interface Remote : OpenAiGenerationDataSource {
        fun validateApiKey(): Single<Boolean>
        fun textToImage(payload: TextToImagePayload): Single<AiGenerationResult>
    }
}
