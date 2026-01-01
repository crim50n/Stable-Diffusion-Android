package dev.minios.pdaiv1.demo.di

import dev.minios.pdaiv1.demo.ImageToImageDemoImpl
import dev.minios.pdaiv1.demo.TextToImageDemoImpl
import dev.minios.pdaiv1.demo.serialize.DemoDataSerializer
import dev.minios.pdaiv1.domain.demo.ImageToImageDemo
import dev.minios.pdaiv1.domain.demo.TextToImageDemo
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val demoModule = module {
    singleOf(::DemoDataSerializer)
    factoryOf(::TextToImageDemoImpl) bind TextToImageDemo::class
    factoryOf(::ImageToImageDemoImpl) bind ImageToImageDemo::class
}
