package dev.minios.pdaiv1.presentation.navigation.router.main

import dev.minios.pdaiv1.presentation.model.LaunchSource
import dev.minios.pdaiv1.presentation.navigation.NavigationEffect
import dev.minios.pdaiv1.presentation.navigation.router.Router

interface MainRouter : Router<NavigationEffect> {

    fun navigateBack()

    fun navigateToOnBoarding(source: LaunchSource)

    fun navigateToPostSplashConfigLoader()

    fun navigateToHomeScreen()

    fun navigateToServerSetup(source: LaunchSource)

    fun navigateToGalleryDetails(itemId: Long)

    fun navigateToGalleryFull()

    fun navigateToImageEditor(itemId: Long)

    fun navigateToReportImage(itemId: Long)

    fun navigateToInPaint()

    fun navigateToDonate()

    fun navigateToDebugMenu()

    fun navigateToLogger()
}
