package dev.minios.pdaiv1.app.di

import dev.minios.pdaiv1.feature.auth.di.authModule
import dev.minios.pdaiv1.feature.diffusion.di.diffusionModule
import dev.minios.pdaiv1.feature.mediapipe.di.mediaPipeModule
import dev.minios.pdaiv1.feature.qnn.di.qnnModule

val featureModule = arrayOf(
    authModule,
    diffusionModule,
    mediaPipeModule,
    qnnModule,
)
