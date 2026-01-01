package dev.minios.pdaiv1.presentation.navigation.router.main

import dev.minios.pdaiv1.domain.usecase.splash.SplashNavigationUseCase
import dev.minios.pdaiv1.presentation.model.LaunchSource

fun MainRouter.postSplashNavigation(
    action: SplashNavigationUseCase.Action,
) {
    when (action) {
        SplashNavigationUseCase.Action.LAUNCH_ONBOARDING -> navigateToOnBoarding(
            source = LaunchSource.SPLASH,
        )
        SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP -> navigateToServerSetup(
            source = LaunchSource.SPLASH,
        )
        SplashNavigationUseCase.Action.LAUNCH_HOME -> navigateToPostSplashConfigLoader()
    }
}
