package com.shifthackz.aisdv1.presentation.model

/**
 * Represents available resolutions for QNN models.
 * These are based on compiled models and patch files.
 * Base model is 512x512, other resolutions require .patch files.
 */
enum class QnnResolution(
    val width: Int,
    val height: Int,
    val patchFileName: String? = null,
    val cpuOnly: Boolean = false,
) {
    // CPU-only resolutions (for MNN models)
    RES_256x256(256, 256, null, cpuOnly = true),
    RES_256x384(256, 384, null, cpuOnly = true),
    RES_384x256(384, 256, null, cpuOnly = true),
    RES_384x384(384, 384, null, cpuOnly = true),

    // NPU resolutions (for QNN models, also available for CPU)
    RES_512x512(512, 512, null),
    RES_512x768(512, 768, "512x768.patch"),
    RES_768x512(768, 512, "768x512.patch"),
    RES_768x768(768, 768, "768.patch"),
    RES_768x1024(768, 1024, "768x1024.patch"),
    RES_1024x768(1024, 768, "1024x768.patch"),
    RES_1024x1024(1024, 1024, "1024.patch");

    val displayName: String
        get() = "${width}×${height}"

    companion object {
        /**
         * Default resolution for QNN NPU models.
         */
        val DEFAULT = RES_512x512

        /**
         * Default resolution for CPU/MNN models.
         */
        val DEFAULT_CPU = RES_256x256

        /**
         * Find QnnResolution by width and height.
         * Returns null if resolution is not supported.
         */
        fun fromDimensions(width: Int, height: Int): QnnResolution? {
            return entries.find { it.width == width && it.height == height }
        }

        /**
         * Check if given dimensions are valid for QNN.
         */
        fun isValidResolution(width: Int, height: Int): Boolean {
            return fromDimensions(width, height) != null
        }

        /**
         * Get resolutions available for NPU models (excludes CPU-only resolutions).
         */
        fun npuResolutions(): List<QnnResolution> = entries.filter { !it.cpuOnly }

        /**
         * Get resolutions available for CPU/MNN models (all resolutions are available).
         */
        fun cpuResolutions(): List<QnnResolution> = entries.toList()

        /**
         * Get resolutions for specific model type.
         * @param runOnCpu If true, returns CPU resolutions; otherwise returns NPU resolutions.
         */
        fun forModelType(runOnCpu: Boolean): List<QnnResolution> {
            return if (runOnCpu) cpuResolutions() else npuResolutions()
        }

        /**
         * Get default resolution for specific model type.
         */
        fun defaultForModelType(runOnCpu: Boolean): QnnResolution {
            return if (runOnCpu) DEFAULT_CPU else DEFAULT
        }

        /**
         * Get all available resolutions as dimension strings.
         */
        val availableDimensionStrings: List<String>
            get() = entries.map { "${it.width}x${it.height}" }
    }
}
