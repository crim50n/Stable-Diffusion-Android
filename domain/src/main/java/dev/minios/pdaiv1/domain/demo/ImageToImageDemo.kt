package dev.minios.pdaiv1.domain.demo

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import io.reactivex.rxjava3.core.Single

fun interface ImageToImageDemo {
    fun getDemoBase64(payload: ImageToImagePayload): Single<AiGenerationResult>
}
