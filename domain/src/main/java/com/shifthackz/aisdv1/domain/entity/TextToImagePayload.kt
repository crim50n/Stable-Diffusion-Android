package com.shifthackz.aisdv1.domain.entity

import java.io.Serializable

data class TextToImagePayload(
    val prompt: String,
    val negativePrompt: String,
    val samplingSteps: Int,
    val cfgScale: Float,
    val distilledCfgScale: Float = 3.5f,
    val width: Int,
    val height: Int,
    val restoreFaces: Boolean,
    val seed: String,
    val subSeed: String,
    val subSeedStrength: Float,
    val sampler: String,
    val scheduler: Scheduler = Scheduler.AUTOMATIC,
    val nsfw: Boolean,
    val batchCount: Int,
    val style: String?,
    val quality: String?,
    val openAiModel: OpenAiModel?,
    val stabilityAiClipGuidance: StabilityAiClipGuidance?,
    val stabilityAiStylePreset: StabilityAiStylePreset?,
    val aDetailer: ADetailerConfig = ADetailerConfig.DISABLED,
    val hires: HiresConfig = HiresConfig.DISABLED,
    val forgeModules: List<ForgeModule> = emptyList(),
) : Serializable
