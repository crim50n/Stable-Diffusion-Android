package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.LocalDiffusionStatus
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Repository for Qualcomm QNN (NPU) based local image generation.
 */
interface QnnGenerationRepository {
    fun observeStatus(): Observable<LocalDiffusionStatus>
    fun generateFromText(payload: TextToImagePayload): Single<AiGenerationResult>
    fun generateFromImage(payload: ImageToImagePayload): Single<AiGenerationResult>
    fun interruptGeneration(): Completable
}
