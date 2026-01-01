package dev.minios.pdaiv1.presentation.modal.download

import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalModelUseCase
import io.reactivex.rxjava3.kotlin.subscribeBy

class DownloadDialogViewModel(
    private val getLocalModelUseCase: GetLocalModelUseCase,
    private val schedulersProvider: SchedulersProvider,
    dispatchersProvider: DispatchersProvider,
) : MviRxViewModel<DownloadDialogState, DownloadDialogIntent, DownloadDialogEffect>() {

    override val initialState = DownloadDialogState()

    override val effectDispatcher = dispatchersProvider.immediate

    override fun processIntent(intent: DownloadDialogIntent) {
        when (intent) {
            is DownloadDialogIntent.LoadModelData -> !getLocalModelUseCase(intent.id)
                .subscribeOnMainThread(schedulersProvider)
                .subscribeBy(::errorLog) { model ->
                    // For QNN models with single source, auto-start download
                    if (model.sources.size == 1) {
                        emitEffect(DownloadDialogEffect.StartDownload(model.sources.first()))
                        emitEffect(DownloadDialogEffect.Close)
                    } else {
                        updateState {
                            it.copy(sources = model.sources.mapIndexed { i, url -> url to (i == 0) })
                        }
                    }
                }

            is DownloadDialogIntent.SelectSource -> updateState {
                it.copy(sources = it.sources.map { (url, _) -> url to (url == intent.url) })
            }

            DownloadDialogIntent.StartDownload -> emitEffect(
                DownloadDialogEffect.StartDownload(currentState.selectedUrl)
            )

            DownloadDialogIntent.Close -> emitEffect(DownloadDialogEffect.Close)
        }
    }
}
