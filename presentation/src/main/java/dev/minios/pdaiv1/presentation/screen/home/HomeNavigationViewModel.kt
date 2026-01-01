package dev.minios.pdaiv1.presentation.screen.home

import dev.minios.pdaiv1.core.common.extensions.EmptyLambda
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.presentation.core.GenerationFormUpdateEvent
import dev.minios.pdaiv1.presentation.navigation.NavigationEffect
import dev.minios.pdaiv1.presentation.navigation.NavigationRoute
import dev.minios.pdaiv1.presentation.navigation.router.home.HomeRouter
import com.shifthackz.android.core.mvi.EmptyState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit

class HomeNavigationViewModel(
    generationFormUpdateEvent: GenerationFormUpdateEvent,
    preferenceManager: PreferenceManager,
    dispatchersProvider: DispatchersProvider,
    schedulersProvider: SchedulersProvider,
    private val homeRouter: HomeRouter,
) : MviRxViewModel<EmptyState, HomeNavigationIntent, NavigationEffect.Home>() {

    override val initialState = EmptyState

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        !homeRouter
            .observe()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda) { effect ->
                (effect as? NavigationEffect.Home.Route)?.let(::emitEffect)
            }

        !generationFormUpdateEvent
            .observeRoute()
            .map(AiGenerationResult.Type::mapToRoute)
            .map(HomeNavigationIntent::Route)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::processIntent)

        !generationFormUpdateEvent
            .observeFalAiRoute()
            .map { NavigationRoute.HomeNavigation.FalAi }
            .map(HomeNavigationIntent::Route)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::processIntent)

        !Observable
            .timer(250L, TimeUnit.MILLISECONDS)
            .flatMapCompletable { preferenceManager.refresh() }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog)
    }

    override fun processIntent(intent: HomeNavigationIntent) {
        when (intent) {
            is HomeNavigationIntent.Route -> homeRouter.navigateToRoute(intent.navRoute)
            is HomeNavigationIntent.Update -> homeRouter.updateExternallyWithoutNavigation(intent.navRoute)
        }
    }
}
