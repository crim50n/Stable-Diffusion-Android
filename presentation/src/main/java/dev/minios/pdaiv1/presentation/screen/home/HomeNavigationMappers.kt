package dev.minios.pdaiv1.presentation.screen.home

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.presentation.navigation.NavigationRoute

fun AiGenerationResult.Type.mapToRoute(): NavigationRoute = when (this) {
    AiGenerationResult.Type.TEXT_TO_IMAGE -> NavigationRoute.HomeNavigation.TxtToImg
    AiGenerationResult.Type.IMAGE_TO_IMAGE -> NavigationRoute.HomeNavigation.ImgToImg
}
