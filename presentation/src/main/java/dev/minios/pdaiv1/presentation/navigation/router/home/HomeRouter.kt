package dev.minios.pdaiv1.presentation.navigation.router.home

import dev.minios.pdaiv1.presentation.navigation.NavigationEffect
import dev.minios.pdaiv1.presentation.navigation.NavigationRoute
import dev.minios.pdaiv1.presentation.navigation.router.Router

interface HomeRouter : Router<NavigationEffect.Home> {

    fun updateExternallyWithoutNavigation(navRoute: NavigationRoute)

    fun navigateToRoute(navRoute: NavigationRoute)

    fun navigateToTxt2Img()

    fun navigateToImg2Img()

    fun navigateToGallery()

    fun navigateToSettings()
}
