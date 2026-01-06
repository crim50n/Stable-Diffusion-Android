package dev.minios.pdaiv1.presentation.navigation.graph

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.minios.pdaiv1.presentation.model.LaunchSource
import dev.minios.pdaiv1.presentation.navigation.LocalAnimatedVisibilityScope
import dev.minios.pdaiv1.presentation.navigation.NavigationRoute
import dev.minios.pdaiv1.presentation.screen.debug.DebugMenuScreen
import dev.minios.pdaiv1.presentation.screen.donate.DonateScreen
import dev.minios.pdaiv1.presentation.screen.gallery.detail.GalleryDetailScreen
import dev.minios.pdaiv1.presentation.screen.gallery.editor.ImageEditorScreen
import dev.minios.pdaiv1.presentation.screen.gallery.list.GalleryScreen
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

@OptIn(ExperimentalSharedTransitionApi::class)
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

    // Full Gallery screen (for Immich-style shared element transitions)
    composable<NavigationRoute.GalleryFull> {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            GalleryScreen()
        }
    }

    // Gallery Detail - transparent transitions for Immich-style overlay effect
    // The gallery stays visible underneath during navigation
    composable<NavigationRoute.GalleryDetail>(
        enterTransition = { fadeIn(animationSpec = tween(150)) },
        exitTransition = { fadeOut(animationSpec = tween(150)) },
        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) },
    ) { entry ->
        // Provide AnimatedVisibilityScope for shared element transitions
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            val itemId = entry.toRoute<NavigationRoute.GalleryDetail>().itemId
            GalleryDetailScreen(itemId = itemId)
        }
    }

    composable<NavigationRoute.ImageEditor>(
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
    ) { entry ->
        val itemId = entry.toRoute<NavigationRoute.ImageEditor>().itemId
        ImageEditorScreen(itemId = itemId)
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
