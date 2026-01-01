package dev.minios.pdaiv1.feature.qnn.api.model

import com.google.gson.annotations.SerializedName

/**
 * Request for /generate endpoint.
 * Supports both txt2img and img2img (when image field is provided).
 *
 * Based on local-dream native server implementation.
 */
data class GenerateRequest(
    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("negative_prompt")
    val negativePrompt: String = "",

    @SerializedName("width")
    val width: Int = 512,

    @SerializedName("height")
    val height: Int = 512,

    @SerializedName("steps")
    val steps: Int = 20,

    @SerializedName("cfg")
    val cfg: Float = 7.0f,

    // Seed for random generation. Use null for random seed (server will generate)
    @SerializedName("seed")
    val seed: Long? = null,

    @SerializedName("scheduler")
    val scheduler: String = "dpm",

    @SerializedName("use_opencl")
    val useOpencl: Boolean = false,

    @SerializedName("show_diffusion_process")
    val showDiffusionProcess: Boolean = false,

    @SerializedName("show_diffusion_stride")
    val showDiffusionStride: Int = 1,

    @SerializedName("denoise_strength")
    val denoiseStrength: Float = 0.6f,

    // For img2img - base64 encoded image
    @SerializedName("image")
    val image: String? = null,

    // For inpainting - base64 encoded mask
    @SerializedName("mask")
    val mask: String? = null
)

/**
 * SSE event data for progress updates.
 */
data class ProgressEvent(
    @SerializedName("type")
    val type: String, // "progress", "complete", "error"

    @SerializedName("step")
    val step: Int = 0,

    @SerializedName("total_steps")
    val totalSteps: Int = 0,

    @SerializedName("image")
    val image: String? = null // Base64 encoded - preview or final image
)

/**
 * SSE event data for completed generation.
 */
data class CompleteEvent(
    @SerializedName("type")
    val type: String = "complete",

    @SerializedName("image")
    val image: String, // Base64 encoded PNG

    @SerializedName("seed")
    val seed: Long,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("channels")
    val channels: Int = 3,

    @SerializedName("generation_time_ms")
    val generationTimeMs: Long,

    @SerializedName("first_step_time_ms")
    val firstStepTimeMs: Long
)

/**
 * SSE event data for errors.
 */
data class ErrorEvent(
    @SerializedName("type")
    val type: String = "error",

    @SerializedName("message")
    val message: String
)
