package com.shifthackz.aisdv1.presentation.screen.txt2img

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.validation.ValidationResult
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.ADetailerConfig
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.ModelType
import com.shifthackz.aisdv1.domain.entity.OpenAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiQuality
import com.shifthackz.aisdv1.domain.entity.OpenAiSize
import com.shifthackz.aisdv1.domain.entity.OpenAiStyle
import com.shifthackz.aisdv1.domain.entity.Scheduler
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiClipGuidance
import com.shifthackz.aisdv1.domain.entity.StabilityAiStylePreset
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.presentation.core.GenerationMviState
import com.shifthackz.aisdv1.presentation.model.FalAiEndpointUi
import com.shifthackz.aisdv1.presentation.model.Modal
import com.shifthackz.aisdv1.core.localization.R as LocalizationR

@Immutable
data class TextToImageState(
    override val onBoardingDemo: Boolean = false,
    override val screenModal: Modal = Modal.None,
    override val mode: ServerSource = ServerSource.AUTOMATIC1111,
    override val modelType: ModelType = ModelType.SD_1_5,
    override val advancedToggleButtonVisible: Boolean = true,
    override val advancedOptionsVisible: Boolean = false,
    override val formPromptTaggedInput: Boolean = false,
    override val prompt: String = "",
    override val negativePrompt: String = "",
    override val width: String = 512.toString(),
    override val height: String = 512.toString(),
    override val samplingSteps: Int = 20,
    override val cfgScale: Float = 7f,
    override val distilledCfgScale: Float = 3.5f,
    override val restoreFaces: Boolean = false,
    override val seed: String = "",
    override val subSeed: String = "",
    override val subSeedStrength: Float = 0f,
    override val selectedSampler: String = "",
    override val selectedScheduler: Scheduler = Scheduler.AUTOMATIC,
    override val availableForgeModules: List<ForgeModule> = emptyList(),
    override val selectedForgeModules: List<ForgeModule> = emptyList(),
    override val aDetailerConfig: ADetailerConfig = ADetailerConfig.DISABLED,
    override val hiresConfig: HiresConfig = HiresConfig.DISABLED,
    override val selectedStylePreset: StabilityAiStylePreset = StabilityAiStylePreset.NONE,
    override val selectedClipGuidancePreset: StabilityAiClipGuidance = StabilityAiClipGuidance.NONE,
    override val openAiModel: OpenAiModel = OpenAiModel.DALL_E_2,
    override val openAiSize: OpenAiSize = OpenAiSize.W1024_H1024,
    override val openAiQuality: OpenAiQuality = OpenAiQuality.STANDARD,
    override val openAiStyle: OpenAiStyle = OpenAiStyle.VIVID,
    override val availableSamplers: List<String> = emptyList(),
    override val widthValidationError: UiText? = null,
    override val heightValidationError: UiText? = null,
    override val nsfw: Boolean = false,
    override val batchCount: Int = 1,
    override val generateButtonEnabled: Boolean = true,
    override val falAiEndpoints: List<FalAiEndpointUi> = emptyList(),
    override val falAiSelectedEndpoint: FalAiEndpointUi? = null,
    override val falAiPropertyValues: Map<String, Any?> = emptyMap(),
    override val falAiAdvancedVisible: Boolean = false,
) : GenerationMviState() {

    override fun copyState(
        onBoardingDemo: Boolean,
        screenModal: Modal,
        mode: ServerSource,
        modelType: ModelType,
        advancedToggleButtonVisible: Boolean,
        advancedOptionsVisible: Boolean,
        formPromptTaggedInput: Boolean,
        prompt: String,
        negativePrompt: String,
        width: String,
        height: String,
        samplingSteps: Int,
        cfgScale: Float,
        distilledCfgScale: Float,
        restoreFaces: Boolean,
        seed: String,
        subSeed: String,
        subSeedStrength: Float,
        selectedSampler: String,
        availableSamplers: List<String>,
        selectedScheduler: Scheduler,
        availableForgeModules: List<ForgeModule>,
        selectedForgeModules: List<ForgeModule>,
        aDetailerConfig: ADetailerConfig,
        hiresConfig: HiresConfig,
        selectedStylePreset: StabilityAiStylePreset,
        selectedClipGuidancePreset: StabilityAiClipGuidance,
        openAiModel: OpenAiModel,
        openAiSize: OpenAiSize,
        openAiQuality: OpenAiQuality,
        openAiStyle: OpenAiStyle,
        widthValidationError: UiText?,
        heightValidationError: UiText?,
        nsfw: Boolean,
        batchCount: Int,
        generateButtonEnabled: Boolean,
        falAiEndpoints: List<FalAiEndpointUi>,
        falAiSelectedEndpoint: FalAiEndpointUi?,
        falAiPropertyValues: Map<String, Any?>,
        falAiAdvancedVisible: Boolean,
    ): GenerationMviState = copy(
        onBoardingDemo = onBoardingDemo,
        screenModal = screenModal,
        mode = mode,
        modelType = modelType,
        advancedToggleButtonVisible = advancedToggleButtonVisible,
        advancedOptionsVisible = advancedOptionsVisible,
        formPromptTaggedInput = formPromptTaggedInput,
        prompt = prompt,
        negativePrompt = negativePrompt,
        width = width,
        height = height,
        samplingSteps = samplingSteps,
        cfgScale = cfgScale,
        distilledCfgScale = distilledCfgScale,
        restoreFaces = restoreFaces,
        seed = seed,
        subSeed = subSeed,
        subSeedStrength = subSeedStrength,
        selectedSampler = selectedSampler,
        availableSamplers = availableSamplers,
        selectedScheduler = selectedScheduler,
        availableForgeModules = availableForgeModules,
        selectedForgeModules = selectedForgeModules,
        aDetailerConfig = aDetailerConfig,
        hiresConfig = hiresConfig,
        selectedStylePreset = selectedStylePreset,
        selectedClipGuidancePreset = selectedClipGuidancePreset,
        openAiModel = openAiModel,
        openAiSize = openAiSize,
        openAiQuality = openAiQuality,
        openAiStyle = openAiStyle,
        widthValidationError = widthValidationError,
        heightValidationError = heightValidationError,
        nsfw = nsfw,
        batchCount = batchCount,
        generateButtonEnabled = generateButtonEnabled,
        falAiEndpoints = falAiEndpoints,
        falAiSelectedEndpoint = falAiSelectedEndpoint,
        falAiPropertyValues = falAiPropertyValues,
        falAiAdvancedVisible = falAiAdvancedVisible,
    )
}

fun TextToImageState.mapToPayload(): TextToImagePayload = with(this) {
    TextToImagePayload(
        prompt = prompt.trim(),
        negativePrompt = negativePrompt.trim(),
        samplingSteps = samplingSteps,
        cfgScale = cfgScale,
        distilledCfgScale = distilledCfgScale,
        width = when (mode) {
            ServerSource.OPEN_AI -> openAiSize.width
            else -> width.toIntOrNull() ?: 64
        },
        height = when (mode) {
            ServerSource.OPEN_AI -> openAiSize.height
            else -> height.toIntOrNull() ?: 64
        },
        restoreFaces = restoreFaces,
        seed = seed.trim(),
        subSeed = subSeed.trim(),
        subSeedStrength = subSeedStrength,
        sampler = selectedSampler,
        scheduler = selectedScheduler,
        nsfw = if (mode == ServerSource.HORDE) nsfw else false,
        batchCount = if (mode == ServerSource.LOCAL_MICROSOFT_ONNX) 1 else batchCount,
        style = openAiStyle.key.takeIf {
            mode == ServerSource.OPEN_AI && openAiModel == OpenAiModel.DALL_E_3
        },
        quality = openAiQuality.key.takeIf {
            mode == ServerSource.OPEN_AI && openAiModel == OpenAiModel.DALL_E_3
        },
        openAiModel = openAiModel.takeIf { mode == ServerSource.OPEN_AI },
        stabilityAiClipGuidance = selectedClipGuidancePreset.takeIf { mode == ServerSource.STABILITY_AI },
        stabilityAiStylePreset = selectedStylePreset.takeIf { mode == ServerSource.STABILITY_AI },
        aDetailer = aDetailerConfig.takeIf { mode == ServerSource.AUTOMATIC1111 } ?: ADetailerConfig.DISABLED,
        hires = hiresConfig.takeIf { mode == ServerSource.AUTOMATIC1111 } ?: HiresConfig.DISABLED,
        forgeModules = selectedForgeModules.takeIf { mode == ServerSource.AUTOMATIC1111 } ?: emptyList(),
    )
}

fun ValidationResult<DimensionValidator.Error>.mapToUi(): UiText? {
    if (this.isValid) return null
    return when (validationError as DimensionValidator.Error) {
        DimensionValidator.Error.Empty -> LocalizationR.string.error_empty.asUiText()
        is DimensionValidator.Error.LessThanMinimum -> UiText.Resource(
            LocalizationR.string.error_min_size,
            (validationError as DimensionValidator.Error.LessThanMinimum).min,
        )
        is DimensionValidator.Error.BiggerThanMaximum -> UiText.Resource(
            LocalizationR.string.error_max_size,
            (validationError as DimensionValidator.Error.BiggerThanMaximum).max,
        )
        else -> LocalizationR.string.error_invalid.asUiText()
    }
}
