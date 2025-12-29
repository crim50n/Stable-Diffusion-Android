package com.shifthackz.aisdv1.presentation.screen.falai

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersProvider
import com.shifthackz.aisdv1.core.common.schedulers.subscribeOnMainThread
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.notification.PushNotificationManager
import com.shifthackz.aisdv1.core.viewmodel.MviRxViewModel
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.FalAiEndpoint
import com.shifthackz.aisdv1.domain.entity.FalAiPayload
import com.shifthackz.aisdv1.domain.entity.FalAiPropertyType
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.FalAiEndpointRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.presentation.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.model.Modal
import com.shifthackz.aisdv1.presentation.model.toUi
import com.shifthackz.aisdv1.presentation.navigation.router.drawer.DrawerRouter
import com.shifthackz.aisdv1.presentation.screen.drawer.DrawerIntent
import io.reactivex.rxjava3.kotlin.subscribeBy
import com.shifthackz.aisdv1.core.localization.R as LocalizationR

class FalAiGenerationViewModel(
    dispatchersProvider: DispatchersProvider,
    private val falAiEndpointRepository: FalAiEndpointRepository,
    private val falAiGenerationRepository: FalAiGenerationRepository,
    private val saveGenerationResultUseCase: SaveGenerationResultUseCase,
    private val schedulersProvider: SchedulersProvider,
    private val preferenceManager: PreferenceManager,
    private val wakeLockInterActor: WakeLockInterActor,
    private val notificationManager: PushNotificationManager,
    private val drawerRouter: DrawerRouter,
    private val buildInfoProvider: BuildInfoProvider,
    private val backgroundTaskManager: BackgroundTaskManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val generationFormUpdateEvent: GenerationFormUpdateEvent,
) : MviRxViewModel<FalAiGenerationState, FalAiGenerationIntent, FalAiGenerationEffect>() {

    override val initialState = FalAiGenerationState()

    override val effectDispatcher = dispatchersProvider.immediate

    private var selectedEndpointDomain: FalAiEndpoint? = null

    init {
        loadEndpoints()
        observeFormUpdates()
    }

    private fun observeFormUpdates() {
        !generationFormUpdateEvent.observeFalAiForm()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onNext = { payload ->
                    when (payload) {
                        is GenerationFormUpdateEvent.Payload.FalAiForm -> {
                            applyFormFromGallery(payload.ai)
                        }
                        else -> Unit
                    }
                },
            )
    }

    private fun applyFormFromGallery(ai: AiGenerationResult) {
        // Extract endpoint ID from sampler field (format: "fal.ai/endpoint-id")
        val endpointId = ai.sampler.removePrefix("fal.ai/")

        // First select the endpoint, then apply saved values on top
        !falAiEndpointRepository.getAll()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onSuccess = { endpoints ->
                    val endpoint = endpoints.find { it.id == endpointId || it.endpointId == endpointId }

                    // Use found endpoint or fall back to currently selected endpoint
                    val targetEndpoint = endpoint ?: selectedEndpointDomain

                    if (targetEndpoint != null) {
                        if (endpoint != null) {
                            selectedEndpointDomain = endpoint
                            preferenceManager.falAiSelectedEndpointId = endpoint.id
                        }

                        val endpointUi = targetEndpoint.toUi()
                        val defaultValues = targetEndpoint.schema.inputProperties
                            .associate { prop -> prop.name to prop.default }
                            .toMutableMap()

                        // Apply saved values from gallery
                        defaultValues["prompt"] = ai.prompt
                        if (ai.negativePrompt.isNotBlank()) {
                            defaultValues["negative_prompt"] = ai.negativePrompt
                        }
                        if (ai.seed.isNotBlank()) {
                            defaultValues["seed"] = ai.seed.toLongOrNull()
                        }

                        // Apply image_size if endpoint supports custom dimensions
                        val imageSizeProperty = targetEndpoint.schema.inputProperties.find { it.name == "image_size" }
                        if (imageSizeProperty != null && imageSizeProperty.type == FalAiPropertyType.IMAGE_SIZE) {
                            defaultValues["image_size"] = mapOf("width" to ai.width, "height" to ai.height)
                        }

                        val allProperties = endpointUi.properties.map { prop ->
                            val savedValue = defaultValues[prop.name]
                            prop.copy(currentValue = savedValue ?: prop.defaultValue)
                        }

                        val mainProps = allProperties.filter { !it.isAdvanced }
                        val advancedProps = allProperties.filter { it.isAdvanced }

                        updateState { state ->
                            state.copy(
                                selectedEndpoint = endpointUi,
                                propertyValues = defaultValues,
                                mainProperties = mainProps,
                                advancedProperties = advancedProps,
                            )
                        }
                    } else {
                        // No endpoint available, just update prompt in current state
                        updateState { state ->
                            val updatedValues = state.propertyValues.toMutableMap()
                            updatedValues["prompt"] = ai.prompt
                            if (ai.negativePrompt.isNotBlank()) {
                                updatedValues["negative_prompt"] = ai.negativePrompt
                            }
                            state.copy(propertyValues = updatedValues)
                        }
                    }

                    // Clear the form update event
                    generationFormUpdateEvent.clear()
                },
            )
    }

    override fun processIntent(intent: FalAiGenerationIntent) {
        when (intent) {
            is FalAiGenerationIntent.SelectEndpoint -> selectEndpoint(intent.endpointId)
            is FalAiGenerationIntent.UpdateProperty -> updateProperty(intent.name, intent.value)
            is FalAiGenerationIntent.ToggleAdvancedOptions -> toggleAdvancedOptions(intent.visible)
            is FalAiGenerationIntent.Generate -> generate()
            is FalAiGenerationIntent.SetModal -> setModal(intent.modal)
            is FalAiGenerationIntent.DismissModal -> dismissModal()
            is FalAiGenerationIntent.Drawer -> when (intent.intent) {
                DrawerIntent.Open -> drawerRouter.openDrawer()
                DrawerIntent.Close -> drawerRouter.closeDrawer()
            }
            is FalAiGenerationIntent.ImportOpenApiJson -> importOpenApiJson(intent.json)
            is FalAiGenerationIntent.DeleteEndpoint -> deleteEndpoint(intent.endpointId)
        }
    }

    private fun loadEndpoints() {
        !falAiEndpointRepository.getAll()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    errorLog(t)
                    updateState { it.copy(loading = false) }
                },
                onSuccess = { endpoints ->
                    val uiEndpoints = endpoints.map { it.toUi() }
                    val selectedId = preferenceManager.falAiSelectedEndpointId
                    val selected = endpoints.find { it.id == selectedId }
                        ?: endpoints.find { it.endpointId == selectedId }
                        ?: endpoints.firstOrNull()

                    selectedEndpointDomain = selected

                    val selectedUi = selected?.toUi()

                    // Start with default values
                    val defaultValues = selected?.schema?.inputProperties
                        ?.associate { prop -> prop.name to prop.default }
                        ?.toMutableMap()
                        ?: mutableMapOf()

                    // Overlay saved params on top of defaults
                    if (selected != null) {
                        val savedParams = preferenceManager.getFalAiEndpointParams(selected.endpointId)
                        savedParams.forEach { (key, value) ->
                            if (value != null) {
                                defaultValues[key] = value
                            }
                        }
                    }

                    val allProperties = selectedUi?.properties?.map { prop ->
                        prop.copy(currentValue = defaultValues[prop.name] ?: prop.defaultValue)
                    } ?: emptyList()

                    val mainProps = allProperties.filter { !it.isAdvanced }
                    val advancedProps = allProperties.filter { it.isAdvanced }

                    updateState { state ->
                        state.copy(
                            loading = false,
                            endpoints = uiEndpoints,
                            selectedEndpoint = selectedUi,
                            propertyValues = defaultValues,
                            mainProperties = mainProps,
                            advancedProperties = advancedProps,
                        )
                    }
                },
            )
    }

    private fun selectEndpoint(endpointId: String) {
        !falAiEndpointRepository.getAll()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onSuccess = { endpoints ->
                    val endpoint = endpoints.find { it.id == endpointId || it.endpointId == endpointId }
                    if (endpoint != null) {
                        selectedEndpointDomain = endpoint
                        preferenceManager.falAiSelectedEndpointId = endpoint.id

                        val endpointUi = endpoint.toUi()

                        // Start with default values
                        val defaultValues = endpoint.schema.inputProperties
                            .associate { prop -> prop.name to prop.default }
                            .toMutableMap()

                        // Overlay saved params on top of defaults
                        val savedParams = preferenceManager.getFalAiEndpointParams(endpoint.endpointId)
                        savedParams.forEach { (key, value) ->
                            if (value != null) {
                                defaultValues[key] = value
                            }
                        }

                        val allProperties = endpointUi.properties.map { prop ->
                            prop.copy(currentValue = defaultValues[prop.name] ?: prop.defaultValue)
                        }

                        val mainProps = allProperties.filter { !it.isAdvanced }
                        val advancedProps = allProperties.filter { it.isAdvanced }

                        updateState { state ->
                            state.copy(
                                selectedEndpoint = endpointUi,
                                propertyValues = defaultValues,
                                mainProperties = mainProps,
                                advancedProperties = advancedProps,
                            )
                        }
                    }
                },
            )
    }

    private fun updateProperty(name: String, value: Any?) {
        updateState { state ->
            val newPropertyValues = state.propertyValues.toMutableMap().apply {
                put(name, value)
            }

            val updatedMainProperties = state.mainProperties.map { prop ->
                if (prop.name == name) prop.copy(currentValue = value) else prop
            }

            val updatedAdvancedProperties = state.advancedProperties.map { prop ->
                if (prop.name == name) prop.copy(currentValue = value) else prop
            }

            state.copy(
                propertyValues = newPropertyValues,
                mainProperties = updatedMainProperties,
                advancedProperties = updatedAdvancedProperties,
            )
        }
    }

    private fun toggleAdvancedOptions(visible: Boolean) {
        updateState { it.copy(advancedOptionsVisible = visible) }
    }

    private fun setModal(modal: Modal) {
        updateState { it.copy(screenModal = modal) }
    }

    private fun dismissModal() {
        updateState { it.copy(screenModal = Modal.None) }
    }

    private fun importOpenApiJson(json: String) {
        !falAiEndpointRepository.importFromJson(json)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    errorLog(t)
                    updateState { state ->
                        state.copy(
                            screenModal = Modal.Error(
                                (t.localizedMessage ?: "Failed to import endpoint").asUiText()
                            )
                        )
                    }
                },
                onSuccess = { endpoint ->
                    // Reload endpoints and select the new one
                    loadEndpoints()
                    selectEndpoint(endpoint.id)
                },
            )
    }

    private fun deleteEndpoint(endpointId: String) {
        !falAiEndpointRepository.delete(endpointId)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    errorLog(t)
                    updateState { state ->
                        state.copy(
                            screenModal = Modal.Error(
                                (t.localizedMessage ?: "Failed to delete endpoint").asUiText()
                            )
                        )
                    }
                },
                onComplete = {
                    loadEndpoints()
                },
            )
    }

    private fun generate() {
        val endpoint = selectedEndpointDomain ?: return
        val parameters = currentState.propertyValues

        // Save parameters for this endpoint
        preferenceManager.saveFalAiEndpointParams(endpoint.endpointId, parameters)

        if (backgroundWorkObserver.hasActiveTasks()) {
            updateState { it.copy(screenModal = Modal.Background.Running) }
            return
        }

        if (preferenceManager.backgroundGeneration) {
            generateBackground(endpoint.endpointId, parameters)
            return
        }

        generateOnUi(endpoint, parameters)
    }

    private fun generateBackground(endpointId: String, parameters: Map<String, Any?>) {
        val payload = FalAiPayload(
            endpointId = endpointId,
            parameters = parameters,
        )
        backgroundTaskManager.scheduleFalAiTask(payload)
        backgroundWorkObserver.refreshStatus()
        updateState { it.copy(screenModal = Modal.Background.Scheduled) }
    }

    private fun generateOnUi(endpoint: FalAiEndpoint, parameters: Map<String, Any?>) {
        !falAiGenerationRepository.generateDynamic(endpoint, parameters)
            .doOnSubscribe {
                wakeLockInterActor.acquireWakelockUseCase()
                updateState { it.copy(generating = true, screenModal = Modal.Communicating()) }
            }
            .doFinally {
                wakeLockInterActor.releaseWakeLockUseCase()
            }
            .flatMapCompletable { results ->
                if (preferenceManager.autoSaveAiResults) {
                    val saveCompletables = results.map { saveGenerationResultUseCase(it) }
                    io.reactivex.rxjava3.core.Completable.merge(saveCompletables)
                        .doOnComplete {
                            updateState { state ->
                                state.copy(
                                    generating = false,
                                    screenModal = Modal.Image.create(
                                        list = results,
                                        autoSaveEnabled = true,
                                        reportEnabled = buildInfoProvider.type != BuildType.FOSS,
                                    ),
                                )
                            }
                        }
                } else {
                    io.reactivex.rxjava3.core.Completable.fromAction {
                        updateState { state ->
                            state.copy(
                                generating = false,
                                screenModal = Modal.Image.create(
                                    list = results,
                                    autoSaveEnabled = false,
                                    reportEnabled = buildInfoProvider.type != BuildType.FOSS,
                                ),
                            )
                        }
                    }
                }
            }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    errorLog(t)
                    notificationManager.createAndShowInstant(
                        LocalizationR.string.notification_fail_title.asUiText(),
                        LocalizationR.string.notification_fail_sub_title.asUiText(),
                    )
                    updateState { state ->
                        state.copy(
                            generating = false,
                            screenModal = Modal.Error(
                                (t.localizedMessage ?: "Generation failed").asUiText()
                            ),
                        )
                    }
                },
                onComplete = {
                    notificationManager.createAndShowInstant(
                        LocalizationR.string.notification_finish_title.asUiText(),
                        LocalizationR.string.notification_finish_sub_title.asUiText(),
                    )
                },
            )
    }
}
