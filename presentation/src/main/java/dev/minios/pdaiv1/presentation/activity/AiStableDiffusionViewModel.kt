package dev.minios.pdaiv1.presentation.activity

import dev.minios.pdaiv1.core.common.extensions.EmptyLambda
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.presentation.navigation.NavigationEffect
import dev.minios.pdaiv1.presentation.navigation.graph.mainDrawerNavItems
import dev.minios.pdaiv1.presentation.navigation.router.drawer.DrawerRouter
import dev.minios.pdaiv1.presentation.navigation.router.home.HomeRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import io.reactivex.rxjava3.kotlin.subscribeBy

class AiStableDiffusionViewModel(
    dispatchersProvider: DispatchersProvider,
    schedulersProvider: SchedulersProvider,
    mainRouter: MainRouter,
    drawerRouter: DrawerRouter,
    private val homeRouter: HomeRouter,
    private val preferenceManager: PreferenceManager,
) : MviRxViewModel<AppState, AppIntent, NavigationEffect>() {

    override val initialState = AppState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        !mainRouter.observe()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::emitEffect)

        !drawerRouter.observe()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::emitEffect)

        !homeRouter.observe()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::emitEffect)

        !preferenceManager.observe()
            .map(::mainDrawerNavItems)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda) { drawerItems ->
                updateState { state ->
                    state.copy(drawerItems = drawerItems)
                }
            }
    }

    override fun processIntent(intent: AppIntent) = when (intent) {
        AppIntent.GrantStoragePermission -> {
            preferenceManager.saveToMediaStore = true
        }

        is AppIntent.HomeRoute -> {
            homeRouter.navigateToRoute(intent.navRoute)
        }

        AppIntent.HideSplash -> updateState { state ->
            state.copy(isShowSplash = false)
        }
    }
}
