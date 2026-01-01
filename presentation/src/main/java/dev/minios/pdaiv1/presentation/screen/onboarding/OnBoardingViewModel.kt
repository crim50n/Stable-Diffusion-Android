package dev.minios.pdaiv1.presentation.screen.onboarding

import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.entity.DarkThemeToken
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.splash.SplashNavigationUseCase
import dev.minios.pdaiv1.presentation.model.LaunchSource
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.postSplashNavigation
import com.shifthackz.android.core.mvi.EmptyEffect
import io.reactivex.rxjava3.kotlin.subscribeBy

class OnBoardingViewModel(
    val launchSource: LaunchSource,
    dispatchersProvider: DispatchersProvider,
    private val mainRouter: MainRouter,
    private val splashNavigationUseCase: SplashNavigationUseCase,
    private val preferenceManager: PreferenceManager,
    private val schedulersProvider: SchedulersProvider,
    private val buildInfoProvider: BuildInfoProvider,
) : MviRxViewModel<OnBoardingState, OnBoardingIntent, EmptyEffect>() {

    override val initialState = OnBoardingState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        updateState {
            val token = DarkThemeToken.parse(preferenceManager.designDarkThemeToken)
            val version = buildInfoProvider.toString()
            it.copy(
                darkThemeToken = token,
                appVersion = version
            )
        }
    }

    override fun processIntent(intent: OnBoardingIntent) {
        when (intent) {
            OnBoardingIntent.Navigate -> {
                preferenceManager.onBoardingComplete = true
                when (launchSource) {
                    LaunchSource.SPLASH -> !splashNavigationUseCase()
                        .subscribeOnMainThread(schedulersProvider)
                        .subscribeBy(::errorLog) { action ->
                            mainRouter.postSplashNavigation(action)
                        }

                    LaunchSource.SETTINGS -> mainRouter.navigateBack()
                }
            }
        }
    }
}
