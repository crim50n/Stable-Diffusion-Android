package dev.minios.pdaiv1.domain.entity

/**
 * Represents the type of Stable Diffusion model being used.
 * Different model types have different generation parameters:
 * - SD_1_5: Standard Stable Diffusion 1.x, uses CFG Scale and negative prompt
 * - SDXL: Stable Diffusion XL, uses CFG Scale and negative prompt
 * - FLUX: Flux models, uses Distilled CFG Scale, no negative prompt support
 */
enum class ModelType(val displayName: String) {
    SD_1_5("SD 1.5"),
    SDXL("SDXL"),
    FLUX("Flux");

    companion object {
        /**
         * Whether this model type supports negative prompts.
         */
        fun ModelType.supportsNegativePrompt(): Boolean = this != FLUX

        /**
         * Whether this model type uses distilled CFG scale instead of regular CFG.
         */
        fun ModelType.usesDistilledCfg(): Boolean = this == FLUX

        /**
         * Returns the default CFG Scale for this model type.
         * Flux uses 1.0, SD/SDXL use 7.0.
         */
        fun ModelType.defaultCfgScale(): Float = when (this) {
            FLUX -> 1.0f
            SD_1_5 -> 7.0f
            SDXL -> 5.0f
        }

        /**
         * Returns the default Distilled CFG Scale for Flux models.
         */
        fun ModelType.defaultDistilledCfgScale(): Float = 3.5f

        /**
         * Returns the default sampler for this model type.
         */
        fun ModelType.defaultSampler(): String = when (this) {
            SD_1_5 -> "Euler a"
            SDXL -> "DPM++ 2M SDE"
            FLUX -> "Euler"
        }

        /**
         * Returns the default width for this model type.
         */
        fun ModelType.defaultWidth(): Int = when (this) {
            SD_1_5 -> 448
            SDXL -> 896
            FLUX -> 896
        }

        /**
         * Returns the default height for this model type.
         */
        fun ModelType.defaultHeight(): Int = when (this) {
            SD_1_5 -> 576
            SDXL -> 1152
            FLUX -> 1152
        }

        /**
         * Returns the default scheduler for this model type.
         */
        fun ModelType.defaultScheduler(): Scheduler = when (this) {
            SD_1_5 -> Scheduler.AUTOMATIC
            SDXL -> Scheduler.KARRAS
            FLUX -> Scheduler.SIMPLE
        }
    }
}
