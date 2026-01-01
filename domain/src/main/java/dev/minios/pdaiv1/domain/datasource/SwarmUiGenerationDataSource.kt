package dev.minios.pdaiv1.domain.datasource

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Single

sealed interface SwarmUiGenerationDataSource {

    interface Remote : SwarmUiGenerationDataSource {
        fun textToImage(
            sessionId: String,
            model: String,
            payload: TextToImagePayload,
        ): Single<AiGenerationResult>

        fun imageToImage(
            sessionId: String,
            model: String,
            payload: ImageToImagePayload,
        ): Single<AiGenerationResult>
    }
}
