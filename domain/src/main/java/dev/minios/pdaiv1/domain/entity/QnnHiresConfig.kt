package dev.minios.pdaiv1.domain.entity

import java.io.Serializable

/**
 * Configuration for Hires.Fix in QNN NPU mode.
 *
 * Hires.Fix for QNN works by:
 * 1. Generating image at base resolution (512x512)
 * 2. Upscaling to target resolution (768 or 1024 variants)
 * 3. Running img2img pass at target resolution for detail refinement
 *
 * Only available for NPU mode. CPU/GPU models are trained on 512 max.
 *
 * @param enabled Whether Hires.Fix is enabled.
 * @param targetWidth Target width after upscaling (768 or 1024).
 * @param targetHeight Target height after upscaling (768 or 1024).
 * @param steps Number of refinement steps (0 = use same as first pass).
 * @param denoisingStrength Denoising strength for the refinement pass (0.0-1.0).
 */
data class QnnHiresConfig(
    val enabled: Boolean = false,
    val targetWidth: Int = 1024,
    val targetHeight: Int = 1024,
    val steps: Int = 0,
    val denoisingStrength: Float = 0.4f,
) : Serializable {

    companion object {
        val DISABLED = QnnHiresConfig(enabled = false)

        /**
         * Default config with 1024x1024 target.
         */
        val DEFAULT = QnnHiresConfig(
            enabled = false,
            targetWidth = 1024,
            targetHeight = 1024,
            steps = 0,
            denoisingStrength = 0.4f,
        )
    }
}
