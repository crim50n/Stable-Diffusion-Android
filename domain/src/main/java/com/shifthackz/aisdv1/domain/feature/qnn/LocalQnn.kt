package com.shifthackz.aisdv1.domain.feature.qnn

import android.graphics.Bitmap
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Result of QNN generation containing the image and metadata.
 */
data class QnnGenerationResult(
    val bitmap: Bitmap,
    val seed: Long,
    val width: Int,
    val height: Int,
)

/**
 * Feature interface for Qualcomm QNN based Stable Diffusion inference.
 *
 * This runs a local HTTP server (localhost:8081) that handles generation requests.
 * Supports both text-to-image and image-to-image generation.
 */
interface LocalQnn {
    /**
     * Process text-to-image generation request.
     */
    fun processTextToImage(payload: TextToImagePayload): Single<QnnGenerationResult>

    /**
     * Process image-to-image generation request.
     */
    fun processImageToImage(payload: ImageToImagePayload): Single<QnnGenerationResult>

    /**
     * Interrupt current generation.
     */
    fun interrupt(): Completable

    /**
     * Observe generation progress status.
     */
    fun observeStatus(): Observable<LocalDiffusionStatus>

    /**
     * Check if QNN backend is available and running.
     */
    fun isAvailable(): Single<Boolean>

    /**
     * Start the QNN backend service.
     */
    fun startService(): Completable

    /**
     * Stop the QNN backend service.
     */
    fun stopService(): Completable
}
