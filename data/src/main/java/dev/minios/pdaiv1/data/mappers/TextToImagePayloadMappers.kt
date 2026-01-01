package dev.minios.pdaiv1.data.mappers

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.OpenAiModel
import dev.minios.pdaiv1.domain.entity.Scheduler
import dev.minios.pdaiv1.domain.entity.StabilityAiClipGuidance
import dev.minios.pdaiv1.domain.entity.StabilityAiSampler
import dev.minios.pdaiv1.domain.entity.StabilityAiStylePreset
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.network.request.HordeGenerationAsyncRequest
import dev.minios.pdaiv1.network.request.HuggingFaceGenerationRequest
import dev.minios.pdaiv1.network.request.OpenAiRequest
import dev.minios.pdaiv1.network.request.OverrideSettings
import dev.minios.pdaiv1.network.request.StabilityTextToImageRequest
import dev.minios.pdaiv1.network.request.SwarmUiGenerationRequest
import dev.minios.pdaiv1.network.request.TextToImageRequest
import dev.minios.pdaiv1.network.response.SdGenerationResponse
import java.util.Date

//region PAYLOAD --> REQUEST
fun TextToImagePayload.mapToRequest(): TextToImageRequest = with(this) {
    TextToImageRequest(
        prompt = prompt,
        negativePrompt = negativePrompt,
        steps = samplingSteps,
        cfgScale = cfgScale,
        distilledCfgScale = distilledCfgScale,
        width = width,
        height = height,
        restoreFaces = restoreFaces,
        seed = seed.trim().ifEmpty { null },
        subSeed = subSeed.trim().ifEmpty { null },
        subSeedStrength = subSeedStrength,
        samplerIndex = sampler,
        scheduler = scheduler.takeIf { it != Scheduler.AUTOMATIC }?.alias,
        alwaysOnScripts = aDetailer.toAlwaysOnScripts(),
        enableHr = hires.enabled.takeIf { it },
        hrUpscaler = hires.upscaler.takeIf { hires.enabled },
        hrScale = hires.scale.takeIf { hires.enabled },
        hrSecondPassSteps = hires.steps.takeIf { hires.enabled && it > 0 },
        hrCfg = hires.hrCfg?.takeIf { hires.enabled },
        hrDistilledCfg = hires.hrDistilledCfg?.takeIf { hires.enabled },
        hrAdditionalModules = if (hires.enabled) emptyList() else null,
        denoisingStrength = hires.denoisingStrength.takeIf { hires.enabled },
        overrideSettings = forgeModules.takeIf { it.isNotEmpty() }?.let { modules ->
            OverrideSettings(forgeAdditionalModules = modules.map { it.path })
        },
    )
}

fun TextToImagePayload.mapToHordeRequest(): HordeGenerationAsyncRequest = with(this) {
    HordeGenerationAsyncRequest(
        prompt = prompt,
        nsfw = nsfw,
        sourceProcessing = null,
        sourceImage = null,
        params = HordeGenerationAsyncRequest.Params(
            cfgScale = cfgScale,
            width = width,
            height = height,
            steps = samplingSteps,
            seed = seed.trim().ifEmpty { null },
            subSeedStrength = subSeedStrength.takeIf { it >= 0.1 },
        )
    )
}

fun TextToImagePayload.mapToHuggingFaceRequest(): HuggingFaceGenerationRequest = with(this) {
    HuggingFaceGenerationRequest(
        inputs = prompt,
        parameters = buildMap {
            this["width"] = width
            this["height"] = height
            negativePrompt.trim().takeIf(String::isNotBlank)?.let {
                this["negative_prompt"] = it
            }
            seed.trim().takeIf(String::isNotBlank)?.let {
                this["seed"] = it
            }
            this["num_inference_steps"] = samplingSteps
            this["guidance_scale"] = cfgScale
        },
    )
}

fun TextToImagePayload.mapToOpenAiRequest(): OpenAiRequest = with(this) {
    OpenAiRequest(
        prompt = prompt,
        model = openAiModel?.alias ?: OpenAiModel.DALL_E_2.alias,
        size = "${width}x${height}",
        responseFormat = "b64_json",
        quality = quality,
        style = style,
    )
}

fun TextToImagePayload.mapToStabilityAiRequest(): StabilityTextToImageRequest = with(this) {
    StabilityTextToImageRequest(
        height = height,
        width = width,
        textPrompts = buildList {
            addAll(prompt.mapToStabilityPrompt(1.0))
            addAll(negativePrompt.mapToStabilityPrompt(-1.0))
        },
        cfgScale = cfgScale,
        clipGuidancePreset = (stabilityAiClipGuidance ?: StabilityAiClipGuidance.NONE).toString(),
        sampler = sampler
            .takeIf { it != "${StabilityAiSampler.NONE}" }
            .takeIf { StabilityAiSampler.entries.map { s -> "$s" }.contains(it) },
        seed = seed.toLongOrNull()?.coerceIn(0L .. 4294967295L) ?: 0L,
        steps = samplingSteps,
        stylePreset = stabilityAiStylePreset?.takeIf { it != StabilityAiStylePreset.NONE }?.key,
    )
}

fun TextToImagePayload.mapToSwarmUiRequest(
    sessionId: String,
    swarmUiModel: String,
): SwarmUiGenerationRequest = with(this) {
    SwarmUiGenerationRequest(
        sessionId = sessionId,
        model = swarmUiModel,
        initImage = null,
        initImageCreativity = null,
        images = 1,
        prompt = prompt,
        negativePrompt = negativePrompt,
        width = width,
        height = height,
        seed = seed.trim().ifEmpty { null },
        variationSeed = subSeed.trim().ifEmpty { null },
        variationSeedStrength = subSeedStrength.takeIf { it >= 0.1 }?.toString(),
        cfgScale = cfgScale,
        steps = samplingSteps,
    )
}
//endregion

//region RESPONSE --> RESULT
fun Pair<TextToImagePayload, SdGenerationResponse>.mapToAiGenResult(): AiGenerationResult =
    let { (payload, response) ->
        AiGenerationResult(
            id = 0L,
            image = response.images?.firstOrNull() ?: "",
            inputImage = "",
            createdAt = Date(),
            type = AiGenerationResult.Type.TEXT_TO_IMAGE,
            denoisingStrength = 0f,
            prompt = payload.prompt,
            negativePrompt = payload.negativePrompt,
            width = payload.width,
            height = payload.height,
            samplingSteps = payload.samplingSteps,
            cfgScale = payload.cfgScale,
            restoreFaces = payload.restoreFaces,
            sampler = payload.sampler,
            seed = if (payload.seed.trim().isNotEmpty()) payload.seed
            else mapSeedFromRemote(response.info),
            subSeed = if (payload.subSeed.trim().isNotEmpty()) payload.subSeed
            else mapSubSeedFromRemote(response.info),
            subSeedStrength = payload.subSeedStrength,
            hidden = false,
            modelName = payload.modelName,
        )
    }

fun Pair<TextToImagePayload, String>.mapCloudToAiGenResult(): AiGenerationResult =
    let { (payload, base64) ->
        AiGenerationResult(
            id = 0L,
            image = base64,
            inputImage = "",
            createdAt = Date(),
            type = AiGenerationResult.Type.TEXT_TO_IMAGE,
            denoisingStrength = 0f,
            prompt = payload.prompt,
            negativePrompt = payload.negativePrompt,
            width = payload.width,
            height = payload.height,
            samplingSteps = payload.samplingSteps,
            cfgScale = payload.cfgScale,
            restoreFaces = payload.restoreFaces,
            sampler = payload.sampler,
            seed = payload.seed,
            subSeed = payload.subSeed,
            subSeedStrength = payload.subSeedStrength,
            hidden = false,
            modelName = payload.modelName,
        )
    }

fun Pair<TextToImagePayload, String>.mapLocalDiffusionToAiGenResult(): AiGenerationResult =
    let { (payload, base64) ->
        AiGenerationResult(
            id = 0L,
            image = base64,
            inputImage = "",
            createdAt = Date(),
            type = AiGenerationResult.Type.TEXT_TO_IMAGE,
            denoisingStrength = 0f,
            prompt = payload.prompt,
            negativePrompt = payload.negativePrompt,
            width = payload.width,
            height = payload.height,
            samplingSteps = payload.samplingSteps,
            cfgScale = payload.cfgScale,
            restoreFaces = payload.restoreFaces,
            sampler = payload.sampler,
            seed = payload.seed,
            subSeed = payload.subSeed,
            subSeedStrength = payload.subSeedStrength,
            hidden = false,
            modelName = payload.modelName,
        )
    }
//endregion

//region QNN Mappers
sealed interface QnnGenerationPayload {
    val seed: Long
}

data class QnnGenerationData private constructor(
    private val textPayload: TextToImagePayload?,
    private val imagePayload: ImageToImagePayload?,
    val base64: String,
    override val seed: Long,
    val width: Int,
    val height: Int,
) : QnnGenerationPayload {

    constructor(
        payload: TextToImagePayload,
        base64: String,
        seed: Long,
        width: Int,
        height: Int,
    ) : this(payload, null, base64, seed, width, height)

    constructor(
        payload: ImageToImagePayload,
        base64: String,
        seed: Long,
        width: Int,
        height: Int,
    ) : this(null, payload, base64, seed, width, height)

    val isTextToImage: Boolean get() = textPayload != null

    val txt2ImgPayload: TextToImagePayload get() = textPayload!!

    val img2ImgPayload: ImageToImagePayload get() = imagePayload!!
}

fun QnnGenerationData.mapQnnResultToAiGenResult(): AiGenerationResult = if (isTextToImage) {
    AiGenerationResult(
        id = 0L,
        image = base64,
        inputImage = "",
        createdAt = Date(),
        type = AiGenerationResult.Type.TEXT_TO_IMAGE,
        denoisingStrength = 0f,
        prompt = txt2ImgPayload.prompt,
        negativePrompt = txt2ImgPayload.negativePrompt,
        width = width,
        height = height,
        samplingSteps = txt2ImgPayload.samplingSteps,
        cfgScale = txt2ImgPayload.cfgScale,
        restoreFaces = txt2ImgPayload.restoreFaces,
        sampler = txt2ImgPayload.sampler,
        seed = seed.toString(),
        subSeed = txt2ImgPayload.subSeed,
        subSeedStrength = txt2ImgPayload.subSeedStrength,
        hidden = false,
        modelName = txt2ImgPayload.modelName,
    )
} else {
    AiGenerationResult(
        id = 0L,
        image = base64,
        inputImage = img2ImgPayload.base64Image,
        createdAt = Date(),
        type = AiGenerationResult.Type.IMAGE_TO_IMAGE,
        denoisingStrength = img2ImgPayload.denoisingStrength,
        prompt = img2ImgPayload.prompt,
        negativePrompt = img2ImgPayload.negativePrompt,
        width = width,
        height = height,
        samplingSteps = img2ImgPayload.samplingSteps,
        cfgScale = img2ImgPayload.cfgScale,
        restoreFaces = img2ImgPayload.restoreFaces,
        sampler = img2ImgPayload.sampler,
        seed = seed.toString(),
        subSeed = img2ImgPayload.subSeed,
        subSeedStrength = img2ImgPayload.subSeedStrength,
        hidden = false,
        modelName = img2ImgPayload.modelName,
    )
}
//endregion
