package dev.minios.pdaiv1.presentation.screen.donate

import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.usecase.donate.FetchAndGetSupportersUseCase
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import io.reactivex.rxjava3.kotlin.subscribeBy

class DonateViewModel(
    dispatchersProvider: DispatchersProvider,
    schedulersProvider: SchedulersProvider,
    fetchAndGetSupportersUseCase: FetchAndGetSupportersUseCase,
    private val mainRouter: MainRouter,
) : MviRxViewModel<DonateState, DonateIntent, DonateEffect>() {

    override val initialState = DonateState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        !fetchAndGetSupportersUseCase()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    updateState { it.copy(loading = false) }
                    errorLog(t)
                },
                onSuccess = { supporters ->
                    updateState {
                        it.copy(
                            loading = false,
                            supporters = supporters,
                        )
                    }
                },
            )
    }

    override fun processIntent(intent: DonateIntent) {
        when (intent) {
            is DonateIntent.LaunchUrl -> emitEffect(DonateEffect.OpenUrl(intent.url))
            DonateIntent.NavigateBack -> mainRouter.navigateBack()
        }
    }
}
