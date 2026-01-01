package dev.minios.pdaiv1.presentation.di

import dev.minios.pdaiv1.presentation.navigation.router.drawer.DrawerRouter
import dev.minios.pdaiv1.presentation.navigation.router.drawer.DrawerRouterImpl
import dev.minios.pdaiv1.presentation.navigation.router.home.HomeRouter
import dev.minios.pdaiv1.presentation.navigation.router.home.HomeRouterImpl
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouterImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val navigationModule = module {
    singleOf(::MainRouterImpl) bind MainRouter::class
    singleOf(::DrawerRouterImpl) bind DrawerRouter::class
    singleOf(::HomeRouterImpl) bind HomeRouter::class
}
