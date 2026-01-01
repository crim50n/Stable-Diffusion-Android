package dev.minios.pdaiv1.presentation.extensions

import androidx.navigation.NavController
import dev.minios.pdaiv1.presentation.navigation.NavigationRoute

fun NavController.navigatePopUpToCurrent(navRoute: NavigationRoute) {
    navigate(navRoute) {
        currentBackStackEntry?.destination?.route?.let {
            popUpTo(it) { inclusive = true }
        }
    }
}
