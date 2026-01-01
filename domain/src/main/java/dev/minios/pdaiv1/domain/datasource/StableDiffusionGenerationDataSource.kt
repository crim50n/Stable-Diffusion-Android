package dev.minios.pdaiv1.domain.datasource

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

sealed interface StableDiffusionGenerationDataSource {
    interface Remote : StableDiffusionGenerationDataSource {
        fun checkAvailability(): Completable
        fun checkAvailability(url: String): Completable
        fun textToImage(payload: TextToImagePayload): Single<AiGenerationResult>
        fun imageToImage(payload: ImageToImagePayload): Single<AiGenerationResult>
        fun interruptGeneration(): Completable
    }
}
