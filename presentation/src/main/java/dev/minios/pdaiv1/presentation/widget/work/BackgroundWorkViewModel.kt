package dev.minios.pdaiv1.presentation.widget.work

import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.entity.BackgroundWorkResult
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.android.core.mvi.EmptyEffect
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.subscribeBy
import dev.minios.pdaiv1.core.localization.R as LocalizationR

class BackgroundWorkViewModel(
    dispatchersProvider: DispatchersProvider,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val schedulersProvider: SchedulersProvider,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
) : MviRxViewModel<BackgroundWorkState, BackgroundWorkIntent, EmptyEffect>() {

    override val initialState = BackgroundWorkState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        !Flowable.combineLatest(
            backgroundWorkObserver.observeStatus(),
            backgroundWorkObserver.observeResult(),
            ::Pair,
        )
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { (work, result) ->
                updateState { state ->
                    val resultTitle = when (result) {
                        is BackgroundWorkResult.Error -> LocalizationR.string.notification_fail_title.asUiText()
                        is BackgroundWorkResult.Success -> LocalizationR.string.notification_finish_title.asUiText()
                        else -> UiText.empty
                    }
                    (result as? BackgroundWorkResult.Success)
                        ?.ai
                        ?.firstOrNull()
                        ?.image
                        ?.also(::setBitmap)
                    
                    val shouldBeVisible = work.running || result !is BackgroundWorkResult.None
                    // Reset dismissed flag when state changes (new generation or new result)
                    val newDismissed = if (shouldBeVisible != state.visible) false else state.dismissed
                    
                    state.copy(
                        visible = shouldBeVisible && !newDismissed,
                        dismissed = newDismissed,
                        title = if (work.running) work.statusTitle.asUiText() else resultTitle,
                        subTitle = if (work.running) work.statusSubTitle.asUiText() else UiText.empty,
                        isError = !work.running && result is BackgroundWorkResult.Error,
                        bitmap = null,
                    )
                }
            }
    }

    override fun processIntent(intent: BackgroundWorkIntent) {
        when (intent) {
            BackgroundWorkIntent.Dismiss -> {
                updateState { it.copy(visible = false, dismissed = true, bitmap = null) }
            }
        }
    }

    private fun setBitmap(base64: String) = !base64ToBitmapConverter(Base64ToBitmapConverter.Input(base64))
        .map(Base64ToBitmapConverter.Output::bitmap)
        .subscribeOnMainThread(schedulersProvider)
        .subscribeBy(::errorLog) { bmp ->
            updateState { it.copy(bitmap = bmp) }
        }
}
