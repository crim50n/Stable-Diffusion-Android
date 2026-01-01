package dev.minios.pdaiv1.core.imageprocessing.di

import android.graphics.BitmapFactory
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64EncodingConverter
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.core.imageprocessing.R
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val imageProcessingModule = module {

    factory {
        Base64ToBitmapConverter(
            get<SchedulersProvider>().computation,
            BitmapFactory.decodeResource(androidContext().resources, R.drawable.ic_broken),
        )
    }

    factory {
        BitmapToBase64Converter(get<SchedulersProvider>().computation)
    }

    factory {
        Base64EncodingConverter(get<SchedulersProvider>().computation)
    }
}
