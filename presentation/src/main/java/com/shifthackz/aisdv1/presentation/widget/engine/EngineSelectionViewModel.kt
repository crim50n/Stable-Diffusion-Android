package com.shifthackz.aisdv1.presentation.widget.engine

import com.shifthackz.aisdv1.core.common.extensions.EmptyLambda
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.core.common.model.Heptagonal
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersProvider
import com.shifthackz.aisdv1.core.common.schedulers.subscribeOnMainThread
import com.shifthackz.aisdv1.core.viewmodel.MviRxViewModel
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalQnnModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ScanCustomModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchAndGetHuggingFaceModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCase
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCase
import com.shifthackz.android.core.mvi.EmptyEffect
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.subscribeBy

class EngineSelectionViewModel(
    dispatchersProvider: DispatchersProvider,
    fetchAndGetSwarmUiModelsUseCase: FetchAndGetSwarmUiModelsUseCase,
    observeLocalOnnxModelsUseCase: ObserveLocalOnnxModelsUseCase,
    fetchAndGetStabilityAiEnginesUseCase: FetchAndGetStabilityAiEnginesUseCase,
    getHuggingFaceModelsUseCase: FetchAndGetHuggingFaceModelsUseCase,
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

        val localAiModels = observeLocalOnnxModelsUseCase()
            .map { models -> models.filter { it.downloaded || it.id == LocalAiModel.CustomOnnx.id } }
            .onErrorReturn { emptyList() }

        // Combine downloaded QNN models with scanned custom models
        val qnnModels = getLocalQnnModelsUseCase()
            .flatMap { downloadedModels ->
                scanCustomModelsUseCase(LocalAiModel.Type.QNN)
                    .map { scannedModels ->
                        val downloaded = downloadedModels.filter { it.downloaded }
                        // Merge: downloaded models + scanned custom models (exclude old Custom entry)
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
            qnnModels,
            ::Heptagonal,
        )
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onComplete = EmptyLambda,
                onNext = { (config, sdModels, swarmModels, hfModels, stEngines, localModels, qnnModels) ->
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
                                ?: state.selectedLocalAiModelId,
                            qnnModels = qnnModels,
                            selectedQnnModelId = config.localQnnModelId,
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

            ServerSource.LOCAL_MICROSOFT_ONNX -> preferenceManager.localOnnxModelId = intent.value

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
