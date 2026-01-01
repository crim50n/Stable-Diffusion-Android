package com.shifthackz.aisdv1.feature.qnn.di

import com.google.gson.Gson
import com.shifthackz.aisdv1.domain.feature.qnn.LocalQnn
import com.shifthackz.aisdv1.feature.qnn.LocalQnnImpl
import com.shifthackz.aisdv1.feature.qnn.model.QnnModelManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

private const val QNN_QUALIFIER = "qnn"

val qnnModule = module {

    // OkHttp client for QNN local API (used for SSE streaming)
    single<OkHttpClient>(named(QNN_QUALIFIER)) {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES) // Very long timeout for generation
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    // Gson for JSON parsing
    single { Gson() }

    // Model manager
    single { QnnModelManager(androidContext()) }

    // LocalQnn implementation using SSE streaming
    single<LocalQnn> {
        LocalQnnImpl(
            context = androidContext(),
            httpClient = get(named(QNN_QUALIFIER)),
            gson = get(),
            preferenceManager = get(),
            fileProviderDescriptor = get()
        )
    }
}
