package dev.minios.pdaiv1.presentation.navigation.router.drawer

import dev.minios.pdaiv1.presentation.navigation.NavigationEffect
import dev.minios.pdaiv1.presentation.navigation.router.Router

interface DrawerRouter : Router<NavigationEffect.Drawer> {

    fun openDrawer()

    fun closeDrawer()
}
