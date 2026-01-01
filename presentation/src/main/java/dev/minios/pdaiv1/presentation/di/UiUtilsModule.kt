package dev.minios.pdaiv1.presentation.di

import dev.minios.pdaiv1.presentation.core.GenerationFormUpdateEvent
import dev.minios.pdaiv1.presentation.screen.debug.DebugMenuAccessor
import dev.minios.pdaiv1.presentation.screen.gallery.detail.GalleryDetailBitmapExporter
import dev.minios.pdaiv1.presentation.screen.gallery.detail.GalleryDetailSharing
import dev.minios.pdaiv1.presentation.screen.gallery.list.GalleryExporter
import dev.minios.pdaiv1.presentation.screen.inpaint.InPaintStateProducer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val uiUtilsModule = module {
    factoryOf(::GalleryExporter)
    factoryOf(::GalleryDetailBitmapExporter)
    factoryOf(::GalleryDetailSharing)
    singleOf(::GenerationFormUpdateEvent)
    singleOf(::DebugMenuAccessor)
    singleOf(::InPaintStateProducer)
}
