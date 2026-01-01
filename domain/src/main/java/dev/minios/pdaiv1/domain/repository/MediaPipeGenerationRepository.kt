package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.LocalDiffusionStatus
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface MediaPipeGenerationRepository {
    fun generateFromText(payload: TextToImagePayload): Single<AiGenerationResult>
}
