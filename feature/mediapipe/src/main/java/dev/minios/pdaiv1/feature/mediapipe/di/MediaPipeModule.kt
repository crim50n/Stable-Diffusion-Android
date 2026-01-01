package dev.minios.pdaiv1.feature.mediapipe.di

import dev.minios.pdaiv1.domain.feature.mediapipe.MediaPipe
import dev.minios.pdaiv1.feature.mediapipe.MediaPipeImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val mediaPipeModule = module {
    factoryOf(::MediaPipeImpl) bind MediaPipe::class
}
