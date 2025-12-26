package com.shifthackz.aisdv1.network.request

import com.google.gson.annotations.SerializedName

data class TextToImageRequest(
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("negative_prompt")
    val negativePrompt: String,
    @SerializedName("steps")
    val steps: Int,
    @SerializedName("cfg_scale")
    val cfgScale: Float,
    @SerializedName("distilled_cfg_scale")
    val distilledCfgScale: Float? = null,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("restore_faces")
    val restoreFaces: Boolean,
    @SerializedName("seed")
    val seed: String?,
    @SerializedName("subseed")
    val subSeed: String?,
    @SerializedName("subseed_strength")
    val subSeedStrength: Float?,
    @SerializedName("sampler_index")
    val samplerIndex: String,
    @SerializedName("scheduler")
    val scheduler: String? = null,
    @SerializedName("alwayson_scripts")
    val alwaysOnScripts: Map<String, Any>? = null,
    @SerializedName("enable_hr")
    val enableHr: Boolean? = null,
    @SerializedName("hr_upscaler")
    val hrUpscaler: String? = null,
    @SerializedName("hr_scale")
    val hrScale: Float? = null,
    @SerializedName("hr_second_pass_steps")
    val hrSecondPassSteps: Int? = null,
    @SerializedName("hr_cfg")
    val hrCfg: Float? = null,
    @SerializedName("hr_distilled_cfg")
    val hrDistilledCfg: Float? = null,
    @SerializedName("hr_additional_modules")
    val hrAdditionalModules: List<String>? = null,
    @SerializedName("denoising_strength")
    val denoisingStrength: Float? = null,
    @SerializedName("override_settings")
    val overrideSettings: OverrideSettings? = null,
)
