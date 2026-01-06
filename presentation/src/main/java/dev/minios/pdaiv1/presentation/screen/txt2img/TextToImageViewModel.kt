package dev.minios.pdaiv1.presentation.screen.txt2img

import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.core.common.appbuild.BuildType
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.notification.PushNotificationManager
import dev.minios.pdaiv1.core.validation.dimension.DimensionValidator
import dev.minios.pdaiv1.domain.entity.HordeProcessStatus
import dev.minios.pdaiv1.domain.entity.LocalDiffusionStatus
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.feature.diffusion.LocalDiffusion
import dev.minios.pdaiv1.domain.feature.work.BackgroundTaskManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.interactor.wakelock.WakeLockInterActor
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import dev.minios.pdaiv1.domain.usecase.forgemodule.GetForgeModulesUseCase
import dev.minios.pdaiv1.domain.usecase.generation.InterruptGenerationUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.SaveGenerationResultUseCase
import dev.minios.pdaiv1.domain.usecase.generation.TextToImageUseCase
import dev.minios.pdaiv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import dev.minios.pdaiv1.presentation.core.GenerationFormUpdateEvent
import dev.minios.pdaiv1.presentation.core.GenerationMviIntent
import dev.minios.pdaiv1.presentation.core.GenerationMviViewModel
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.navigation.router.drawer.DrawerRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import com.shifthackz.android.core.mvi.EmptyEffect
import io.reactivex.rxjava3.kotlin.subscribeBy
import dev.minios.pdaiv1.core.localization.R as LocalizationR

class TextToImageViewModel(
    dispatchersProvider: DispatchersProvider,
    generationFormUpdateEvent: GenerationFormUpdateEvent,
    getStableDiffusionSamplersUseCase: GetStableDiffusionSamplersUseCase,
    getForgeModulesUseCase: GetForgeModulesUseCase,
    observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    saveLastResultToCacheUseCase: SaveLastResultToCacheUseCase,
    saveGenerationResultUseCase: SaveGenerationResultUseCase,
    interruptGenerationUseCase: InterruptGenerationUseCase,
    mainRouter: MainRouter,
    drawerRouter: DrawerRouter,
    dimensionValidator: DimensionValidator,
    private val textToImageUseCase: TextToImageUseCase,
    private val schedulersProvider: SchedulersProvider,
    private val preferenceManager: PreferenceManager,
    private val notificationManager: PushNotificationManager,
    private val wakeLockInterActor: WakeLockInterActor,
    private val backgroundTaskManager: BackgroundTaskManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val buildInfoProvider: BuildInfoProvider,
) : GenerationMviViewModel<TextToImageState, GenerationMviIntent, EmptyEffect>(
    preferenceManager = preferenceManager,
    getStableDiffusionSamplersUseCase = getStableDiffusionSamplersUseCase,
    getForgeModulesUseCase = getForgeModulesUseCase,
    observeHordeProcessStatusUseCase = observeHordeProcessStatusUseCase,
    observeLocalDiffusionProcessStatusUseCase = observeLocalDiffusionProcessStatusUseCase,
    saveLastResultToCacheUseCase = saveLastResultToCacheUseCase,
    saveGenerationResultUseCase = saveGenerationResultUseCase,
    interruptGenerationUseCase = interruptGenerationUseCase,
    mainRouter = mainRouter,
    drawerRouter = drawerRouter,
    dimensionValidator = dimensionValidator,
    schedulersProvider = schedulersProvider,
    backgroundWorkObserver = backgroundWorkObserver,
) {

    private val progressModal: Modal
        get() = when (currentState.mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> {
                Modal.Generating(canCancel = preferenceManager.localOnnxAllowCancel)
            }
            ServerSource.LOCAL_QUALCOMM_QNN -> {
                Modal.Generating(canCancel = true)
            }

            else -> Modal.Communicating()
        }

    override val initialState = TextToImageState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        !generationFormUpdateEvent
            .observeTxt2ImgForm()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = ::errorLog,
                onNext = { payload ->
                    (payload as? GenerationFormUpdateEvent.Payload.T2IForm)
                        ?.let(::updateFormPreviousAiGeneration)
                        ?.also { generationFormUpdateEvent.clear() }
                },
            )
    }

    override fun generateDisposable() = currentState
        .mapToPayload()
        .let(textToImageUseCase::invoke)
        .doOnSubscribe {
            wakeLockInterActor.acquireWakelockUseCase()
            setActiveModal(progressModal)
        }
        .doFinally { wakeLockInterActor.releaseWakeLockUseCase() }
        .subscribeOnMainThread(schedulersProvider)
        .subscribeBy(
            onError = { t ->
                notificationManager.createAndShowInstant(
                    LocalizationR.string.notification_fail_title.asUiText(),
                    LocalizationR.string.notification_fail_sub_title.asUiText(),
                )
                setActiveModal(
                    Modal.Error(
                        (t.localizedMessage ?: "Something went wrong").asUiText()
                    )
                )
                errorLog(t)
            },
            onSuccess = { ai ->
                notificationManager.createAndShowInstant(
                    LocalizationR.string.notification_finish_title.asUiText(),
                    LocalizationR.string.notification_finish_sub_title.asUiText(),
                )
                setActiveModal(
                    Modal.Image.create(
                        list = ai,
                        autoSaveEnabled = preferenceManager.autoSaveAiResults,
                        reportEnabled = buildInfoProvider.type == BuildType.PLAY,
                    )
                )
            },
        )

    override fun generateBackground() {
        val payload = currentState.mapToPayload()
        backgroundTaskManager.scheduleTextToImageTask(payload)
    }

    override fun onReceivedHordeStatus(status: HordeProcessStatus) {
        (currentState.screenModal as? Modal.Communicating)
            ?.copy(hordeProcessStatus = status)
            ?.let(::setActiveModal)
    }

    override fun onReceivedLocalDiffusionStatus(status: LocalDiffusionStatus) {
        (currentState.screenModal as? Modal.Generating)
            ?.copy(status = status)
            ?.let(::setActiveModal)
    }
}
