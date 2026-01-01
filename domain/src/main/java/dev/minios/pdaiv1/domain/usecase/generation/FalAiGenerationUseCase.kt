package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.FalAiPayload
import io.reactivex.rxjava3.core.Single

interface FalAiGenerationUseCase {
    operator fun invoke(payload: FalAiPayload): Single<List<AiGenerationResult>>
}
