package dev.minios.pdaiv1.presentation.screen.splash

import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.usecase.splash.SplashNavigationUseCase
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.postSplashNavigation
import com.shifthackz.android.core.mvi.EmptyEffect
import com.shifthackz.android.core.mvi.EmptyIntent
import com.shifthackz.android.core.mvi.EmptyState
import io.reactivex.rxjava3.kotlin.subscribeBy

class SplashViewModel(
    mainRouter: MainRouter,
    splashNavigationUseCase: SplashNavigationUseCase,
    dispatchersProvider: DispatchersProvider,
    schedulersProvider: SchedulersProvider,
) : MviRxViewModel<EmptyState, EmptyIntent, EmptyEffect>() {

    override val initialState = EmptyState

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        !splashNavigationUseCase()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { action -> mainRouter.postSplashNavigation(action) }
    }
}
