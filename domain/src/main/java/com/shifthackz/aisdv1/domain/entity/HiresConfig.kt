package com.shifthackz.aisdv1.domain.entity

import java.io.Serializable

/**
 * Configuration for Hires. Fix (High Resolution Fix) in A1111/Forge.
 * Allows upscaling generated images with additional refinement pass.
 *
 * @param enabled Whether Hires. Fix is enabled.
 * @param upscaler The upscaler to use (e.g., "Latent", "R-ESRGAN 4x+").
 * @param scale The upscale factor (default: 2.0).
 * @param steps The number of hires fix steps (0 = use same as first pass).
 * @param denoisingStrength The denoising strength for the second pass.
 * @param hrCfg CFG scale for hires pass (null = use server default based on model).
 * @param hrDistilledCfg Distilled CFG scale for hires pass (null = use server default, used for Flux).
 */
data class HiresConfig(
    val enabled: Boolean = false,
    val upscaler: String = "None",
    val scale: Float = 2.0f,
    val steps: Int = 0,
    val denoisingStrength: Float = 0.4f,
    val hrCfg: Float? = null,
    val hrDistilledCfg: Float? = null,
) : Serializable {

    companion object {
        val DISABLED = HiresConfig(enabled = false)

        val AVAILABLE_UPSCALERS = listOf(
            "Latent",
            "Latent (antialiased)",
            "Latent (bicubic)",
            "Latent (bicubic antialiased)",
            "Latent (nearest)",
            "Latent (nearest-exact)",
            "None",
            "Lanczos",
            "Nearest",
            "ESRGAN_4x",
            "LDSR",
            "R-ESRGAN 4x+",
            "R-ESRGAN 4x+ Anime6B",
            "ScuNET GAN",
            "ScuNET PSNR",
            "SwinIR 4x",
        )
    }
}
