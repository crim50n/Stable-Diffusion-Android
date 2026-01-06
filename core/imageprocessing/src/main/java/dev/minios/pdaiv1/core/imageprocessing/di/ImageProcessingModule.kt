package dev.minios.pdaiv1.core.imageprocessing.di

import android.graphics.BitmapFactory
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64EncodingConverter
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.core.imageprocessing.R
import dev.minios.pdaiv1.core.imageprocessing.ThumbnailGenerator
import dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashDecoder
import dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashEncoder
import dev.minios.pdaiv1.core.imageprocessing.cache.ImageCacheManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val imageProcessingModule = module {

    single {
        ImageCacheManager()
    }

    factory {
        Base64ToBitmapConverter(
            get<SchedulersProvider>().computation,
            BitmapFactory.decodeResource(androidContext().resources, R.drawable.ic_broken),
        )
    }

    factory {
        ThumbnailGenerator(
            processingScheduler = get<SchedulersProvider>().computation,
            imageCacheManager = get(),
            fallbackBitmap = BitmapFactory.decodeResource(androidContext().resources, R.drawable.ic_broken),
        )
    }

    factory {
        BitmapToBase64Converter(get<SchedulersProvider>().computation)
    }

    factory {
        Base64EncodingConverter(get<SchedulersProvider>().computation)
    }

    factory {
        BlurHashEncoder(
            processingScheduler = get<SchedulersProvider>().computation,
        )
    }

    factory {
        BlurHashDecoder(
            processingScheduler = get<SchedulersProvider>().computation,
            fallbackBitmap = BitmapFactory.decodeResource(androidContext().resources, R.drawable.ic_broken),
        )
    }
}
