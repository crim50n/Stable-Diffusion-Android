@file:Suppress("UNCHECKED_CAST")

package dev.minios.pdaiv1.presentation.core

import dev.minios.pdaiv1.core.common.extensions.EmptyLambda
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.validation.dimension.DimensionValidator
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.entity.HordeProcessStatus
import dev.minios.pdaiv1.domain.entity.LocalDiffusionStatus
import dev.minios.pdaiv1.domain.entity.ModelType
import dev.minios.pdaiv1.domain.entity.ModelType.Companion.defaultCfgScale
import dev.minios.pdaiv1.domain.entity.ModelType.Companion.defaultHeight
import dev.minios.pdaiv1.domain.entity.ModelType.Companion.defaultSampler
import dev.minios.pdaiv1.domain.entity.ModelType.Companion.defaultScheduler
import dev.minios.pdaiv1.domain.entity.ModelType.Companion.defaultWidth
import dev.minios.pdaiv1.domain.entity.OpenAiSize
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.entity.StabilityAiSampler
import dev.minios.pdaiv1.domain.entity.StableDiffusionSampler
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import dev.minios.pdaiv1.domain.usecase.forgemodule.GetForgeModulesUseCase
import dev.minios.pdaiv1.domain.usecase.generation.InterruptGenerationUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.SaveGenerationResultUseCase
import dev.minios.pdaiv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import dev.minios.pdaiv1.presentation.model.LaunchSource
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.navigation.router.drawer.DrawerRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.screen.drawer.DrawerIntent
import dev.minios.pdaiv1.presentation.screen.txt2img.mapToUi
import com.shifthackz.android.core.mvi.MviEffect
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit

abstract class GenerationMviViewModel<S : GenerationMviState, I : GenerationMviIntent, E : MviEffect>(
    private val preferenceManager: PreferenceManager,
    getStableDiffusionSamplersUseCase: GetStableDiffusionSamplersUseCase,
    getForgeModulesUseCase: GetForgeModulesUseCase,
    observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    private val saveLastResultToCacheUseCase: SaveLastResultToCacheUseCase,
    private val saveGenerationResultUseCase: SaveGenerationResultUseCase,
    private val interruptGenerationUseCase: InterruptGenerationUseCase,
    private val mainRouter: MainRouter,
    private val drawerRouter: DrawerRouter,
    private val dimensionValidator: DimensionValidator,
    private val schedulersProvider: SchedulersProvider,
    private val backgroundWorkObserver: BackgroundWorkObserver,
) : MviRxViewModel<S, I, E>() {

    private var generationDisposable: Disposable? = null
    private var randomImageDisposable: Disposable? = null

    init {
        !preferenceManager
            .observe()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onComplete = EmptyLambda,
                onNext = { settings ->
                    updateGenerationState {
                        val modelTypeChanged = it.modelType != settings.modelType
                        val sourceChanged = it.mode != settings.source
                        it
                            .copyState(
                                mode = settings.source,
                                modelType = settings.modelType,
                                advancedToggleButtonVisible = !settings.formAdvancedOptionsAlwaysShow,
                                formPromptTaggedInput = settings.formPromptTaggedInput,
                            )
                            .let { state ->
                                // When switching to QNN, set default resolution and samplers
                                if (sourceChanged && settings.source == ServerSource.LOCAL_QUALCOMM_QNN) {
                                    val qnnSamplers = listOf("DPM++ 2M", "Euler a")
                                    val runOnCpu = preferenceManager.localQnnRunOnCpu
                                    val defaultRes = if (runOnCpu) "256" else "512"
                                    state.copyState(
                                        width = defaultRes,
                                        height = defaultRes,
                                        widthValidationError = null,
                                        heightValidationError = null,
                                        availableSamplers = qnnSamplers,
                                        selectedSampler = qnnSamplers.first(),
                                        prompt = preferenceManager.localQnnLastPrompt,
                                        negativePrompt = preferenceManager.localQnnLastNegativePrompt,
                                        qnnRunOnCpu = runOnCpu,
                                    )
                                } else if (settings.source == ServerSource.LOCAL_QUALCOMM_QNN) {
                                    // Already in QNN mode, just update runOnCpu if changed
                                    val runOnCpu = preferenceManager.localQnnRunOnCpu
                                    if (state.qnnRunOnCpu != runOnCpu) {
                                        val defaultRes = if (runOnCpu) "256" else "512"
                                        state.copyState(
                                            qnnRunOnCpu = runOnCpu,
                                            width = defaultRes,
                                            height = defaultRes,
                                        )
                                    } else state
                                } else if (modelTypeChanged) {
                                    state.copyState(
                                        cfgScale = settings.modelType.defaultCfgScale(),
                                        selectedSampler = settings.modelType.defaultSampler(),
                                        selectedScheduler = settings.modelType.defaultScheduler(),
                                        width = settings.modelType.defaultWidth().toString(),
                                        height = settings.modelType.defaultHeight().toString(),
                                    )
                                } else state
                            }
                            .let { state ->
                                if (!settings.formAdvancedOptionsAlwaysShow) state
                                else state.copyState(advancedOptionsVisible = true)
                            }
                            .let { state ->
                                state.copyState(modelName = getModelName(settings.source))
                            }
                    }
                }
            )

        !getStableDiffusionSamplersUseCase()
            .map { samplers -> samplers.map(StableDiffusionSampler::name) }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onSuccess = { samplers ->
                    updateGenerationState { state ->
                        val allSamplers = when (state.mode) {
                            ServerSource.STABILITY_AI -> StabilityAiSampler.entries.map { "$it" }
                            ServerSource.LOCAL_QUALCOMM_QNN -> listOf("DPM++ 2M", "Euler a")
                            else -> samplers
                        }
                        state.copyState(
                            availableSamplers = allSamplers,
                            selectedSampler = allSamplers.firstOrNull() ?: "",
                        )
                    }
                }
            )

        !observeHordeProcessStatusUseCase()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onNext = ::onReceivedHordeStatus,
                onComplete = EmptyLambda,
            )

        !observeLocalDiffusionProcessStatusUseCase()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onNext = ::onReceivedLocalDiffusionStatus,
                onComplete = EmptyLambda,
            )

        !getForgeModulesUseCase()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onSuccess = { modules ->
                    updateGenerationState { state ->
                        state.copyState(availableForgeModules = modules)
                    }
                }
            )
    }

    abstract fun generateDisposable(): Disposable

    abstract fun generateBackground()

    open fun onReceivedHordeStatus(status: HordeProcessStatus) {}

    open fun onReceivedLocalDiffusionStatus(status: LocalDiffusionStatus) {}

    override fun onCleared() {
        super.onCleared()
        generationDisposable?.dispose()
        generationDisposable = null
        randomImageDisposable?.dispose()
        randomImageDisposable = null
    }

    override fun processIntent(intent: I) {
        when (intent) {
            is GenerationMviIntent.NewPrompts -> updateGenerationState {
                it.copyState(
                    prompt = intent.positive.trim(),
                    negativePrompt = intent.negative.trim(),
                )
            }

            is GenerationMviIntent.SetAdvancedOptionsVisibility -> updateGenerationState {
                it.copyState(advancedOptionsVisible = intent.visible)
            }

            is GenerationMviIntent.Update.Prompt -> updateGenerationState {
                it.copyState(prompt = intent.value)
            }

            is GenerationMviIntent.Update.NegativePrompt -> updateGenerationState {
                it.copyState(negativePrompt = intent.value)
            }

            is GenerationMviIntent.Update.Size.Width -> updateGenerationState {
                it.copyState(
                    width = intent.value,
                    widthValidationError = dimensionValidator(intent.value).mapToUi(),
                )
            }

            is GenerationMviIntent.Update.Size.Height -> updateGenerationState {
                it.copyState(
                    height = intent.value,
                    heightValidationError = dimensionValidator(intent.value).mapToUi(),
                )
            }

            is GenerationMviIntent.Update.Size.Swap -> updateGenerationState {
                val newWidth = it.height
                val newHeight = it.width
                it.copyState(
                    width = newWidth,
                    height = newHeight,
                    widthValidationError = dimensionValidator(newWidth).mapToUi(),
                    heightValidationError = dimensionValidator(newHeight).mapToUi(),
                )
            }

            is GenerationMviIntent.Update.Size.AspectRatio -> updateGenerationState {
                val baseSize = maxOf(
                    it.width.toIntOrNull() ?: 512,
                    it.height.toIntOrNull() ?: 512
                )
                val (newWidth, newHeight) = intent.ratio.calculateDimensions(baseSize)
                it.copyState(
                    width = newWidth.toString(),
                    height = newHeight.toString(),
                    widthValidationError = dimensionValidator(newWidth.toString()).mapToUi(),
                    heightValidationError = dimensionValidator(newHeight.toString()).mapToUi(),
                )
            }

            is GenerationMviIntent.Update.SamplingSteps -> updateGenerationState {
                it.copyState(samplingSteps = intent.value)
            }

            is GenerationMviIntent.Update.CfgScale -> updateGenerationState {
                it.copyState(cfgScale = intent.value)
            }

            is GenerationMviIntent.Update.RestoreFaces -> updateGenerationState {
                it.copyState(restoreFaces = intent.value)
            }

            is GenerationMviIntent.Update.Seed -> updateGenerationState {
                it.copyState(seed = intent.value)
            }

            is GenerationMviIntent.Update.SubSeed -> updateGenerationState {
                it.copyState(subSeed = intent.value)
            }

            is GenerationMviIntent.Update.SubSeedStrength -> updateGenerationState {
                it.copyState(subSeedStrength = intent.value)
            }

            is GenerationMviIntent.Update.Sampler -> updateGenerationState {
                it.copyState(selectedSampler = intent.value)
            }

            is GenerationMviIntent.Update.Nsfw -> updateGenerationState {
                it.copyState(nsfw = intent.value)
            }

            is GenerationMviIntent.Update.Batch -> updateGenerationState {
                it.copyState(batchCount = intent.value)
            }

            is GenerationMviIntent.Update.Scheduler -> updateGenerationState {
                it.copyState(selectedScheduler = intent.value)
            }

            is GenerationMviIntent.Update.ADetailer -> updateGenerationState {
                it.copyState(aDetailerConfig = intent.value)
            }

            is GenerationMviIntent.Update.Hires -> updateGenerationState {
                it.copyState(hiresConfig = intent.value)
            }

            is GenerationMviIntent.Update.ForgeModules -> updateGenerationState {
                it.copyState(selectedForgeModules = intent.value)
            }

            is GenerationMviIntent.Update.DistilledCfgScale -> updateGenerationState {
                it.copyState(distilledCfgScale = intent.value)
            }

            is GenerationMviIntent.Update.ModelTypeChange -> {
                preferenceManager.modelType = intent.value
                updateGenerationState {
                    it.copyState(
                        modelType = intent.value,
                        cfgScale = intent.value.defaultCfgScale(),
                        selectedSampler = intent.value.defaultSampler(),
                        selectedScheduler = intent.value.defaultScheduler(),
                        width = intent.value.defaultWidth().toString(),
                        height = intent.value.defaultHeight().toString(),
                    )
                }
            }

            is GenerationMviIntent.Update.OpenAi.Model -> updateGenerationState { state ->
                val size = if (state.openAiSize.supportedModels.contains(intent.value)) {
                    state.openAiSize
                } else {
                    OpenAiSize.entries.first { it.supportedModels.contains(intent.value) }
                }
                state.copyState(openAiModel = intent.value, openAiSize = size)
            }

            is GenerationMviIntent.Update.OpenAi.Size -> updateGenerationState {
                it.copyState(openAiSize = intent.value)
            }

            is GenerationMviIntent.Update.OpenAi.Quality -> updateGenerationState {
                it.copyState(openAiQuality = intent.value)
            }

            is GenerationMviIntent.Update.OpenAi.Style -> updateGenerationState {
                it.copyState(openAiStyle = intent.value)
            }

            is GenerationMviIntent.Update.Qnn.Resolution -> updateGenerationState {
                it.copyState(
                    width = intent.value.width.toString(),
                    height = intent.value.height.toString(),
                    widthValidationError = null,
                    heightValidationError = null,
                )
            }

            is GenerationMviIntent.Result.Save -> !Observable
                .fromIterable(intent.ai)
                .flatMapCompletable(saveGenerationResultUseCase::invoke)
                .subscribeOnMainThread(schedulersProvider)
                .subscribeBy(::errorLog) { setActiveModal(Modal.None) }

            is GenerationMviIntent.Result.View -> !saveLastResultToCacheUseCase(intent.ai)
                .subscribeOnMainThread(schedulersProvider)
                .subscribeBy(::errorLog) { mainRouter.navigateToGalleryDetails(it.id) }

            is GenerationMviIntent.Result.Report -> mainRouter.navigateToReportImage(intent.ai.id)

            is GenerationMviIntent.SetModal -> setActiveModal(intent.modal)

            GenerationMviIntent.Cancel.Generation -> {
                generationDisposable?.dispose()
                generationDisposable = null
                !interruptGenerationUseCase()
                    .doOnSubscribe { setActiveModal(Modal.None) }
                    .subscribeOnMainThread(schedulersProvider)
                    .subscribeBy(::errorLog)
            }

            GenerationMviIntent.Cancel.FetchRandomImage -> {
                randomImageDisposable?.dispose()
                randomImageDisposable = null
                setActiveModal(Modal.None)
            }

            GenerationMviIntent.Generate -> {
                // Auto-save prompts for QNN backend
                if (preferenceManager.source == ServerSource.LOCAL_QUALCOMM_QNN) {
                    val state = currentState as? GenerationMviState
                    state?.let {
                        preferenceManager.localQnnLastPrompt = it.prompt.trim()
                        preferenceManager.localQnnLastNegativePrompt = it.negativePrompt.trim()
                    }
                }

                if (backgroundWorkObserver.hasActiveTasks()) {
                    setActiveModal(Modal.Background.Running)
                } else {
                    if (preferenceManager.backgroundGeneration) {
                        generateBackground()
                        backgroundWorkObserver.refreshStatus()
                        setActiveModal(Modal.Background.Scheduled)
                    } else {
                        generateOnUi { generateDisposable() }
                    }
                }
            }

            GenerationMviIntent.Configuration -> mainRouter.navigateToServerSetup(
                LaunchSource.SETTINGS,
            )

            is GenerationMviIntent.UpdateFromGeneration -> {
                updateFormPreviousAiGeneration(intent.payload)
            }

            is GenerationMviIntent.Drawer -> when (intent.intent) {
                DrawerIntent.Close -> drawerRouter.closeDrawer()
                DrawerIntent.Open -> drawerRouter.openDrawer()
            }

            else -> Unit
        }
    }

    protected open fun updateFormPreviousAiGeneration(payload: GenerationFormUpdateEvent.Payload) {
        val ai = when (payload) {
            is GenerationFormUpdateEvent.Payload.I2IForm -> payload.ai
            is GenerationFormUpdateEvent.Payload.T2IForm -> payload.ai
            else -> return
        }
        updateGenerationState { oldState ->
            oldState
                .copyState(
                    advancedOptionsVisible = true,
                    prompt = ai.prompt,
                    negativePrompt = ai.negativePrompt,
                    width = "${ai.width}",
                    height = "${ai.height}",
                    seed = ai.seed,
                    subSeed = ai.subSeed,
                    subSeedStrength = ai.subSeedStrength,
                    samplingSteps = ai.samplingSteps,
                    cfgScale = ai.cfgScale,
                    restoreFaces = ai.restoreFaces,
                )
                .let { state ->
                    if (!state.availableSamplers.contains(ai.sampler)) state
                    else state.copyState(selectedSampler = ai.sampler)
                }
        }
    }

    protected fun setActiveModal(modal: Modal) = updateGenerationState {
        it.copyState(screenModal = modal)
    }

    protected fun fetchRandomImage(fn: () -> Disposable) {
        randomImageDisposable?.dispose()
        randomImageDisposable = null
        val newDisposable = fn()
        randomImageDisposable = newDisposable
        randomImageDisposable?.addToDisposable()
    }

    private fun generateOnUi(fn: () -> Disposable) {
        generationDisposable?.dispose()
        generationDisposable = null
        val newDisposable = fn()
        generationDisposable = newDisposable
        generationDisposable?.addToDisposable()
    }

    private fun updateGenerationState(mutation: (GenerationMviState) -> GenerationMviState) =
        runCatching {
            updateState(mutation as (S) -> S)
        }

    private fun getModelName(source: ServerSource): String = when (source) {
        ServerSource.AUTOMATIC1111 -> preferenceManager.sdModel
        ServerSource.SWARM_UI -> preferenceManager.swarmUiModel
        ServerSource.LOCAL_MICROSOFT_ONNX -> preferenceManager.localOnnxModelId
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> preferenceManager.localMediaPipeModelId
        ServerSource.LOCAL_QUALCOMM_QNN -> preferenceManager.localQnnModelId
        ServerSource.HORDE -> "Horde"
        ServerSource.HUGGING_FACE -> preferenceManager.huggingFaceModel
        ServerSource.OPEN_AI -> "OpenAI"
        ServerSource.STABILITY_AI -> preferenceManager.stabilityAiEngineId
        ServerSource.FAL_AI -> preferenceManager.falAiSelectedEndpointId
    }
}
