package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Single

interface TextToImageUseCase {
    operator fun invoke(payload: TextToImagePayload): Single<List<AiGenerationResult>>
}
