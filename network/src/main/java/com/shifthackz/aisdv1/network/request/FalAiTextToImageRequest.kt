package com.shifthackz.aisdv1.network.request

import com.google.gson.annotations.SerializedName

data class FalAiTextToImageRequest(
    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("negative_prompt")
    val negativePrompt: String? = null,

    @SerializedName("image_size")
    val imageSize: FalAiImageSize? = null,

    @SerializedName("num_inference_steps")
    val numInferenceSteps: Int? = null,

    @SerializedName("guidance_scale")
    val guidanceScale: Float? = null,

    @SerializedName("seed")
    val seed: Long? = null,

    @SerializedName("num_images")
    val numImages: Int = 1,

    @SerializedName("output_format")
    val outputFormat: String = "jpeg",

    @SerializedName("enable_safety_checker")
    val enableSafetyChecker: Boolean = false,

    @SerializedName("sync_mode")
    val syncMode: Boolean = false,
)

data class FalAiImageSize(
    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int,
)
