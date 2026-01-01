package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import io.reactivex.rxjava3.core.Single

interface ImageToImageUseCase {
    operator fun invoke(payload: ImageToImagePayload): Single<List<AiGenerationResult>>
}
