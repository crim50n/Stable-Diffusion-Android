package dev.minios.pdaiv1.presentation.screen.debug

import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.core.common.log.FileLoggingTree
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.feature.work.BackgroundTaskManager
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.debug.DebugInsertBadBase64UseCase
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import io.reactivex.rxjava3.kotlin.subscribeBy
import dev.minios.pdaiv1.core.localization.R as LocalizationR

class DebugMenuViewModel(
    dispatchersProvider: DispatchersProvider,
    private val preferenceManager: PreferenceManager,
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val debugInsertBadBase64UseCase: DebugInsertBadBase64UseCase,
    private val schedulersProvider: SchedulersProvider,
    private val mainRouter: MainRouter,
    private val backgroundTaskManager: BackgroundTaskManager,
) : MviRxViewModel<DebugMenuState, DebugMenuIntent, DebugMenuEffect>() {

    override val initialState = DebugMenuState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        !preferenceManager
            .observe()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { settings ->
                updateState { state ->
                    state.copy(
                        localDiffusionAllowCancel = settings.localDiffusionAllowCancel,
                        localDiffusionSchedulerThread = settings.localDiffusionSchedulerThread,
                    )
                }
            }
    }

    override fun processIntent(intent: DebugMenuIntent) {
        when (intent) {
            DebugMenuIntent.NavigateBack -> mainRouter.navigateBack()

            DebugMenuIntent.InsertBadBase64 -> !debugInsertBadBase64UseCase()
                .subscribeOnMainThread(schedulersProvider)
                .subscribeBy(::onError, ::onSuccess)

            DebugMenuIntent.ClearLogs -> {
                try {
                    FileLoggingTree.clearLog(fileProviderDescriptor)
                    onSuccess()
                } catch (e: Exception) {
                    onError(e)
                }
            }

            DebugMenuIntent.ViewLogs -> mainRouter.navigateToLogger()

            DebugMenuIntent.AllowLocalDiffusionCancel -> {
                preferenceManager.localOnnxAllowCancel = !currentState.localDiffusionAllowCancel
            }

            DebugMenuIntent.LocalDiffusionScheduler.Request -> updateState {
                it.copy(screenModal = Modal.LDScheduler(it.localDiffusionSchedulerThread))
            }

            is DebugMenuIntent.LocalDiffusionScheduler.Confirm -> {
                preferenceManager.localOnnxSchedulerThread = intent.token
            }

            DebugMenuIntent.DismissModal -> updateState {
                it.copy(screenModal = Modal.None)
            }

            DebugMenuIntent.WorkManager.CancelAll -> backgroundTaskManager
                .cancelAll()
                .handleState()

            DebugMenuIntent.WorkManager.RestartTxt2Img -> backgroundTaskManager
                .retryLastTextToImageTask()
                .handleState()

            DebugMenuIntent.WorkManager.RestartImg2Img -> backgroundTaskManager
                .retryLastImageToImageTask()
                .handleState()
        }
    }

    private fun Result<Unit>.handleState() = this.fold(
        onSuccess = { onSuccess() },
        onFailure = ::onError,
    )

    private fun onSuccess() {
        emitEffect(DebugMenuEffect.Message(LocalizationR.string.success.asUiText()))
    }

    private fun onError(t: Throwable) {
        errorLog(t)
        emitEffect(DebugMenuEffect.Message(LocalizationR.string.failure.asUiText()))
    }
}
