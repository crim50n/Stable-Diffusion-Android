package dev.minios.pdaiv1.presentation.screen.web.webui

import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import com.shifthackz.android.core.mvi.EmptyEffect

class WebUiViewModel(
    dispatchersProvider: DispatchersProvider,
    private val mainRouter: MainRouter,
    private val preferenceManager: PreferenceManager,
) : MviRxViewModel<WebUiState, WebUiIntent, EmptyEffect>() {

    override val initialState: WebUiState = WebUiState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        updateState { state ->
            state.copy(
                loading = false,
                source = preferenceManager.source,
                url = when (preferenceManager.source) {
                    ServerSource.AUTOMATIC1111 -> preferenceManager.automatic1111ServerUrl
                    ServerSource.SWARM_UI -> preferenceManager.swarmUiServerUrl
                    else -> ""
                }
            )
        }
    }

    override fun processIntent(intent: WebUiIntent) {
        when (intent) {
            WebUiIntent.NavigateBack -> mainRouter.navigateBack()
        }
    }
}
