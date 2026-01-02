package dev.minios.pdaiv1.presentation.core

import android.graphics.Bitmap
import dev.minios.pdaiv1.domain.entity.ADetailerConfig
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ForgeModule
import dev.minios.pdaiv1.domain.entity.HiresConfig
import dev.minios.pdaiv1.domain.entity.ModelType
import dev.minios.pdaiv1.domain.entity.OpenAiModel
import dev.minios.pdaiv1.domain.entity.OpenAiQuality
import dev.minios.pdaiv1.domain.entity.OpenAiSize
import dev.minios.pdaiv1.domain.entity.OpenAiStyle
import dev.minios.pdaiv1.domain.entity.QnnHiresConfig
import dev.minios.pdaiv1.domain.entity.StabilityAiClipGuidance
import dev.minios.pdaiv1.domain.entity.StabilityAiStylePreset
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.model.QnnResolution
import dev.minios.pdaiv1.presentation.screen.drawer.DrawerIntent
import com.shifthackz.android.core.mvi.MviIntent

sealed interface GenerationMviIntent : MviIntent {

    data class NewPrompts(
        val positive: String,
        val negative: String,
    ) : GenerationMviIntent

    data class SetAdvancedOptionsVisibility(val visible: Boolean) : GenerationMviIntent

    sealed interface Update : GenerationMviIntent {

        data class Prompt(val value: String) : Update

        data class NegativePrompt(val value: String) : Update

        sealed interface Size : Update {

            data class Width(val value: String) : Size

            data class Height(val value: String) : Size

            data object Swap : Size

            data class AspectRatio(val ratio: dev.minios.pdaiv1.presentation.model.AspectRatio) : Size
        }

        data class SamplingSteps(val value: Int) : Update

        data class CfgScale(val value: Float) : Update

        data class DistilledCfgScale(val value: Float) : Update

        data class ModelTypeChange(val value: ModelType) : Update

        data class RestoreFaces(val value: Boolean) : Update

        data class Seed(val value: String) : Update

        data class SubSeed(val value: String) : Update

        data class SubSeedStrength(val value: Float) : Update

        data class Sampler(val value: String) : Update

        data class Nsfw(val value: Boolean) : Update

        data class Batch(val value: Int) : Update

        data class Scheduler(val value: dev.minios.pdaiv1.domain.entity.Scheduler) : Update

        data class ADetailer(val value: ADetailerConfig) : Update

        data class Hires(val value: HiresConfig) : Update

        data class ForgeModules(val value: List<ForgeModule>) : Update

        sealed interface OpenAi : Update {

            data class Model(val value: OpenAiModel) : OpenAi

            data class Size(val value: OpenAiSize) : OpenAi

            data class Quality(val value: OpenAiQuality) : OpenAi

            data class Style(val value: OpenAiStyle) : OpenAi
        }

        sealed interface StabilityAi : Update {
            data class Style(val value: StabilityAiStylePreset) : StabilityAi

            data class ClipGuidance(val value: StabilityAiClipGuidance) : StabilityAi
        }

        sealed interface FalAi : Update {
            data class SelectEndpoint(val endpointId: String) : FalAi
            data class UpdateProperty(val name: String, val value: Any?) : FalAi
            data class ToggleAdvanced(val visible: Boolean) : FalAi
        }

        sealed interface Qnn : Update {
            data class Resolution(val value: QnnResolution) : Qnn
            data class Hires(val value: QnnHiresConfig) : Qnn
        }
    }

    sealed interface Result : GenerationMviIntent {

        data class Save(val ai: List<AiGenerationResult>) : Result

        data class View(val ai: AiGenerationResult) : Result

        data class Report(val ai: AiGenerationResult) : Result
    }

    data class SetModal(val modal: Modal) : GenerationMviIntent

    enum class Cancel : GenerationMviIntent {
        Generation, FetchRandomImage,
    }

    data object Configuration : GenerationMviIntent

    data object Generate : GenerationMviIntent

    data class UpdateFromGeneration(
        val payload: GenerationFormUpdateEvent.Payload,
    ) : GenerationMviIntent

    data class Drawer(val intent: DrawerIntent) : GenerationMviIntent
}

sealed interface ImageToImageIntent : GenerationMviIntent {

    data object InPaint : ImageToImageIntent

    data object FetchRandomPhoto : ImageToImageIntent

    data object ClearImageInput : ImageToImageIntent

    data class UpdateDenoisingStrength(val value: Float) : ImageToImageIntent

    data class UpdateImage(val bitmap: Bitmap) : ImageToImageIntent

    data class CropImage(val bitmap: Bitmap) : ImageToImageIntent

    enum class Pick : ImageToImageIntent {
        Camera, Gallery
    }
}
