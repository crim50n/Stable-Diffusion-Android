package dev.minios.pdaiv1.presentation.core

import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.domain.entity.ADetailerConfig
import dev.minios.pdaiv1.domain.entity.ForgeModule
import dev.minios.pdaiv1.domain.entity.HiresConfig
import dev.minios.pdaiv1.domain.entity.ModelType
import dev.minios.pdaiv1.domain.entity.QnnHiresConfig
import dev.minios.pdaiv1.domain.entity.OpenAiModel
import dev.minios.pdaiv1.domain.entity.OpenAiQuality
import dev.minios.pdaiv1.domain.entity.OpenAiSize
import dev.minios.pdaiv1.domain.entity.OpenAiStyle
import dev.minios.pdaiv1.domain.entity.Scheduler
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.entity.StabilityAiClipGuidance
import dev.minios.pdaiv1.domain.entity.StabilityAiStylePreset
import dev.minios.pdaiv1.presentation.model.FalAiEndpointUi
import dev.minios.pdaiv1.presentation.model.Modal
import com.shifthackz.android.core.mvi.MviState

abstract class GenerationMviState : MviState {
    abstract val onBoardingDemo: Boolean
    abstract val screenModal: Modal
    abstract val mode: ServerSource
    abstract val modelType: ModelType
    abstract val advancedToggleButtonVisible: Boolean
    abstract val advancedOptionsVisible: Boolean
    abstract val formPromptTaggedInput: Boolean
    abstract val prompt: String
    abstract val negativePrompt: String
    abstract val width: String
    abstract val height: String
    abstract val samplingSteps: Int
    abstract val cfgScale: Float
    abstract val distilledCfgScale: Float
    abstract val restoreFaces: Boolean
    abstract val seed: String
    abstract val subSeed: String
    abstract val subSeedStrength: Float
    abstract val selectedSampler: String
    abstract val availableSamplers: List<String>
    abstract val selectedScheduler: Scheduler
    abstract val availableForgeModules: List<ForgeModule>
    abstract val selectedForgeModules: List<ForgeModule>
    abstract val aDetailerConfig: ADetailerConfig
    abstract val hiresConfig: HiresConfig
    abstract val selectedStylePreset: StabilityAiStylePreset
    abstract val selectedClipGuidancePreset: StabilityAiClipGuidance
    abstract val openAiModel: OpenAiModel
    abstract val openAiSize: OpenAiSize
    abstract val openAiQuality: OpenAiQuality
    abstract val openAiStyle: OpenAiStyle
    abstract val widthValidationError: UiText?
    abstract val heightValidationError: UiText?
    abstract val nsfw: Boolean
    abstract val batchCount: Int
    abstract val generateButtonEnabled: Boolean

    // FalAi specific fields
    abstract val falAiEndpoints: List<FalAiEndpointUi>
    abstract val falAiSelectedEndpoint: FalAiEndpointUi?
    abstract val falAiPropertyValues: Map<String, Any?>
    abstract val falAiAdvancedVisible: Boolean

    // QNN specific fields
    abstract val qnnRunOnCpu: Boolean
    abstract val qnnHiresConfig: QnnHiresConfig

    // Model name for saving with generation result
    abstract val modelName: String

    open val promptKeywords: List<String>
        get() = prompt.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    open val negativePromptKeywords: List<String>
        get() = negativePrompt.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    open val hasValidationErrors: Boolean
        get() = widthValidationError != null || heightValidationError != null

    open fun copyState(
        onBoardingDemo: Boolean = this.onBoardingDemo,
        screenModal: Modal = this.screenModal,
        mode: ServerSource = this.mode,
        modelType: ModelType = this.modelType,
        advancedToggleButtonVisible: Boolean = this.advancedToggleButtonVisible,
        advancedOptionsVisible: Boolean = this.advancedOptionsVisible,
        formPromptTaggedInput: Boolean = this.formPromptTaggedInput,
        prompt: String = this.prompt,
        negativePrompt: String = this.negativePrompt,
        width: String = this.width,
        height: String = this.height,
        samplingSteps: Int = this.samplingSteps,
        cfgScale: Float = this.cfgScale,
        distilledCfgScale: Float = this.distilledCfgScale,
        restoreFaces: Boolean = this.restoreFaces,
        seed: String = this.seed,
        subSeed: String = this.subSeed,
        subSeedStrength: Float = this.subSeedStrength,
        selectedSampler: String = this.selectedSampler,
        availableSamplers: List<String> = this.availableSamplers,
        selectedScheduler: Scheduler = this.selectedScheduler,
        availableForgeModules: List<ForgeModule> = this.availableForgeModules,
        selectedForgeModules: List<ForgeModule> = this.selectedForgeModules,
        aDetailerConfig: ADetailerConfig = this.aDetailerConfig,
        hiresConfig: HiresConfig = this.hiresConfig,
        selectedStylePreset: StabilityAiStylePreset = this.selectedStylePreset,
        selectedClipGuidancePreset: StabilityAiClipGuidance = this.selectedClipGuidancePreset,
        openAiModel: OpenAiModel = this.openAiModel,
        openAiSize: OpenAiSize = this.openAiSize,
        openAiQuality: OpenAiQuality = this.openAiQuality,
        openAiStyle: OpenAiStyle = this.openAiStyle,
        widthValidationError: UiText? = this.widthValidationError,
        heightValidationError: UiText? = this.heightValidationError,
        nsfw: Boolean = this.nsfw,
        batchCount: Int = this.batchCount,
        generateButtonEnabled: Boolean = this.generateButtonEnabled,
        falAiEndpoints: List<FalAiEndpointUi> = this.falAiEndpoints,
        falAiSelectedEndpoint: FalAiEndpointUi? = this.falAiSelectedEndpoint,
        falAiPropertyValues: Map<String, Any?> = this.falAiPropertyValues,
        falAiAdvancedVisible: Boolean = this.falAiAdvancedVisible,
        qnnRunOnCpu: Boolean = this.qnnRunOnCpu,
        qnnHiresConfig: QnnHiresConfig = this.qnnHiresConfig,
        modelName: String = this.modelName,
    ): GenerationMviState = this
}
