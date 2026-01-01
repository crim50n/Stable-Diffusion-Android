package dev.minios.pdaiv1.presentation.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.minios.pdaiv1.presentation.model.LaunchSource
import dev.minios.pdaiv1.presentation.navigation.NavigationRoute
import dev.minios.pdaiv1.presentation.screen.debug.DebugMenuScreen
import dev.minios.pdaiv1.presentation.screen.donate.DonateScreen
import dev.minios.pdaiv1.presentation.screen.gallery.detail.GalleryDetailScreen
import dev.minios.pdaiv1.presentation.screen.inpaint.InPaintScreen
import dev.minios.pdaiv1.presentation.screen.loader.ConfigurationLoaderScreen
import dev.minios.pdaiv1.presentation.screen.logger.LoggerScreen
import dev.minios.pdaiv1.presentation.screen.onboarding.OnBoardingScreen
import dev.minios.pdaiv1.presentation.screen.onboarding.OnBoardingViewModel
import dev.minios.pdaiv1.presentation.screen.report.ReportScreen
import dev.minios.pdaiv1.presentation.screen.report.ReportViewModel
import dev.minios.pdaiv1.presentation.screen.setup.ServerSetupScreen
import dev.minios.pdaiv1.presentation.screen.setup.ServerSetupViewModel
import dev.minios.pdaiv1.presentation.screen.splash.SplashScreen
import dev.minios.pdaiv1.presentation.screen.web.webui.WebUiScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.reflect.typeOf

fun NavGraphBuilder.mainNavGraph() {

    composable<NavigationRoute.Splash> {
        SplashScreen()
    }

    composable<NavigationRoute.ServerSetup>(
        typeMap = mapOf(
            typeOf<LaunchSource>() to NavType.EnumType(LaunchSource::class.java)
        )
    ) { entry ->
        val sourceKey = entry.toRoute<NavigationRoute.ServerSetup>().source.ordinal
        ServerSetupScreen(
            viewModel = koinViewModel<ServerSetupViewModel>(
                parameters = { parametersOf(sourceKey) }
            ),
            buildInfoProvider = koinInject()
        )
    }

    composable<NavigationRoute.ConfigLoader> {
        ConfigurationLoaderScreen()
    }

    homeScreenNavGraph()

    composable<NavigationRoute.GalleryDetail> { entry ->
        val itemId = entry.toRoute<NavigationRoute.GalleryDetail>().itemId
        GalleryDetailScreen(itemId = itemId)
    }

    composable<NavigationRoute.ReportImage> { entry ->
        val itemId = entry.toRoute<NavigationRoute.ReportImage>().itemId
        ReportScreen(
            viewModel = koinViewModel<ReportViewModel>(
                parameters = { parametersOf(itemId) }
            ),
        )
    }

    composable<NavigationRoute.Debug> {
        DebugMenuScreen()
    }

    composable<NavigationRoute.Logger> {
        LoggerScreen()
    }

    composable<NavigationRoute.InPaint> {
        InPaintScreen()
    }

    composable<NavigationRoute.WebUi> {
        WebUiScreen()
    }

    composable<NavigationRoute.Donate> {
        DonateScreen()
    }

    composable<NavigationRoute.Onboarding>(
        typeMap = mapOf(
            typeOf<LaunchSource>() to NavType.EnumType(LaunchSource::class.java)
        )
    ) { entry ->
        val sourceKey = entry.toRoute<NavigationRoute.Onboarding>().source.ordinal
        OnBoardingScreen(
            viewModel = koinViewModel<OnBoardingViewModel>(
                parameters = { parametersOf(sourceKey) }
            ),
        )
    }
}
