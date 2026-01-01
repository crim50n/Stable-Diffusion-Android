package com.shifthackz.aisdv1.app.di

import com.shifthackz.aisdv1.feature.auth.di.authModule
import com.shifthackz.aisdv1.feature.diffusion.di.diffusionModule
import com.shifthackz.aisdv1.feature.mediapipe.di.mediaPipeModule
import com.shifthackz.aisdv1.feature.qnn.di.qnnModule

val featureModule = arrayOf(
    authModule,
    diffusionModule,
    mediaPipeModule,
    qnnModule,
)
