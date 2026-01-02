package dev.minios.pdaiv1.presentation.widget.engine

import dev.minios.pdaiv1.core.common.extensions.EmptyLambda
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.model.Octagonal
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.entity.Configuration
import dev.minios.pdaiv1.domain.entity.LocalAiModel
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalQnnModelsUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.ScanCustomModelsUseCase
import dev.minios.pdaiv1.domain.usecase.huggingface.FetchAndGetHuggingFaceModelsUseCase
import dev.minios.pdaiv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import dev.minios.pdaiv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import dev.minios.pdaiv1.domain.usecase.settings.GetConfigurationUseCase
import dev.minios.pdaiv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCase
import dev.minios.pdaiv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCase
import com.shifthackz.android.core.mvi.EmptyEffect
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.subscribeBy

class EngineSelectionViewModel(
    dispatchersProvider: DispatchersProvider,
    fetchAndGetSwarmUiModelsUseCase: FetchAndGetSwarmUiModelsUseCase,
    observeLocalOnnxModelsUseCase: ObserveLocalOnnxModelsUseCase,
    fetchAndGetStabilityAiEnginesUseCase: FetchAndGetStabilityAiEnginesUseCase,
    getHuggingFaceModelsUseCase: FetchAndGetHuggingFaceModelsUseCase,
    getLocalMediaPipeModelsUseCase: GetLocalMediaPipeModelsUseCase,
    getLocalQnnModelsUseCase: GetLocalQnnModelsUseCase,
    scanCustomModelsUseCase: ScanCustomModelsUseCase,
    private val preferenceManager: PreferenceManager,
    private val schedulersProvider: SchedulersProvider,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val selectStableDiffusionModelUseCase: SelectStableDiffusionModelUseCase,
    private val getStableDiffusionModelsUseCase: GetStableDiffusionModelsUseCase,
) : MviRxViewModel<EngineSelectionState, EngineSelectionIntent, EmptyEffect>() {

    override val initialState = EngineSelectionState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        val configuration = preferenceManager
            .observe()
            .flatMap { getConfigurationUseCase().toFlowable() }
            .onErrorReturn { Configuration() }

        val a1111Models = getStableDiffusionModelsUseCase()
            .onErrorReturn { emptyList() }
            .toFlowable()

        val swarmModels = fetchAndGetSwarmUiModelsUseCase()
            .onErrorReturn { emptyList() }
            .toFlowable()

        val huggingFaceModels = getHuggingFaceModelsUseCase()
            .onErrorReturn { emptyList() }
            .toFlowable()

        val stabilityAiEngines = fetchAndGetStabilityAiEnginesUseCase()
            .onErrorReturn { emptyList() }
            .toFlowable()

        // Combine downloaded ONNX models with scanned custom models
        val localAiModels = observeLocalOnnxModelsUseCase()
            .flatMap { downloadedModels ->
                scanCustomModelsUseCase(LocalAiModel.Type.ONNX)
                    .map { scannedModels ->
                        val downloaded = downloadedModels.filter { it.downloaded }
                        downloaded.filter { it.id != LocalAiModel.CustomOnnx.id } + scannedModels
                    }
                    .onErrorReturn { downloadedModels.filter { it.downloaded } }
                    .toFlowable()
            }
            .onErrorReturn { emptyList() }

        // Combine downloaded MediaPipe models with scanned custom models
        val mediaPipeModels = getLocalMediaPipeModelsUseCase()
            .flatMap { downloadedModels ->
                scanCustomModelsUseCase(LocalAiModel.Type.MediaPipe)
                    .map { scannedModels ->
                        val downloaded = downloadedModels.filter { it.downloaded }
                        downloaded.filter { it.id != LocalAiModel.CustomMediaPipe.id } + scannedModels
                    }
                    .onErrorReturn { downloadedModels.filter { it.downloaded } }
            }
            .onErrorReturn { emptyList() }
            .toFlowable()

        // Combine downloaded QNN models with scanned custom models
        val qnnModels = getLocalQnnModelsUseCase()
            .flatMap { downloadedModels ->
                scanCustomModelsUseCase(LocalAiModel.Type.QNN)
                    .map { scannedModels ->
                        val downloaded = downloadedModels.filter { it.downloaded }
                        downloaded.filter { it.id != LocalAiModel.CustomQnn.id } + scannedModels
                    }
                    .onErrorReturn { downloadedModels.filter { it.downloaded } }
            }
            .onErrorReturn { emptyList() }
            .toFlowable()

        !Flowable.combineLatest(
            configuration,
            a1111Models,
            swarmModels,
            huggingFaceModels,
            stabilityAiEngines,
            localAiModels,
            mediaPipeModels,
            qnnModels,
            ::Octagonal,
        )
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onComplete = EmptyLambda,
                onNext = { (config, sdModels, swarmModels, hfModels, stEngines, localModels, mpModels, qnnModels) ->
                    updateState { state ->
                        state.copy(
                            loading = false,
                            mode = config.source,
                            sdModels = sdModels.map { it.first.title },
                            selectedSdModel = sdModels.firstOrNull { it.second }?.first?.title
                                ?: state.selectedSdModel,
                            swarmModels = swarmModels.map { it.name },
                            selectedSwarmModel = swarmModels.firstOrNull { it.name == config.swarmUiModel }?.name
                                ?: state.selectedSwarmModel,
                            hfModels = hfModels.map { it.alias },
                            selectedHfModel = config.huggingFaceModel,
                            stEngines = stEngines.map { it.id },
                            selectedStEngine = config.stabilityAiEngineId,
                            localAiModels = localModels,
                            selectedLocalAiModelId = localModels.firstOrNull { it.id == config.localOnnxModelId }?.id
                                ?: localModels.firstOrNull()?.id
                                ?: state.selectedLocalAiModelId,
                            mediaPipeModels = mpModels,
                            selectedMediaPipeModelId = mpModels.firstOrNull { it.id == config.localMediaPipeModelId }?.id
                                ?: mpModels.firstOrNull()?.id
                                ?: state.selectedMediaPipeModelId,
                            qnnModels = qnnModels,
                            selectedQnnModelId = qnnModels.firstOrNull { it.id == config.localQnnModelId }?.id
                                ?: qnnModels.firstOrNull()?.id
                                ?: state.selectedQnnModelId,
                        )
                    }
                },
            )
    }

    override fun processIntent(intent: EngineSelectionIntent) {
        when (currentState.mode) {
            ServerSource.AUTOMATIC1111 -> !selectStableDiffusionModelUseCase(intent.value)
                .doOnSubscribe {
                    updateState {
                        it.copy(
                            loading = true,
                            selectedSdModel = intent.value,
                        )
                    }
                }
                .andThen(getStableDiffusionModelsUseCase())
                .subscribeOnMainThread(schedulersProvider)
                .subscribeBy(::errorLog) { sdModels ->
                    updateState { state ->
                        state.copy(
                            loading = false,
                            sdModels = sdModels.map { it.first.title },
                            selectedSdModel = sdModels.first { it.second }.first.title,
                        )
                    }
                }

            ServerSource.SWARM_UI -> preferenceManager.swarmUiModel = intent.value

            ServerSource.HUGGING_FACE -> preferenceManager.huggingFaceModel = intent.value

            ServerSource.STABILITY_AI -> preferenceManager.stabilityAiEngineId = intent.value

            ServerSource.LOCAL_MICROSOFT_ONNX -> {
                preferenceManager.localOnnxModelId = intent.value
                updateState { it.copy(selectedLocalAiModelId = intent.value) }
            }

            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> {
                preferenceManager.localMediaPipeModelId = intent.value
                updateState { it.copy(selectedMediaPipeModelId = intent.value) }
            }

            ServerSource.LOCAL_QUALCOMM_QNN -> {
                preferenceManager.localQnnModelId = intent.value
                // Update runOnCpu based on selected model
                val selectedModel = currentState.qnnModels.find { it.id == intent.value }
                if (selectedModel != null) {
                    preferenceManager.localQnnRunOnCpu = selectedModel.runOnCpu
                }
                updateState { it.copy(selectedQnnModelId = intent.value) }
            }

            else -> Unit
        }
    }
}
