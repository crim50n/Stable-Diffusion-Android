package dev.minios.pdaiv1.network.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import dev.minios.pdaiv1.network.api.automatic1111.Automatic1111RestApi
import dev.minios.pdaiv1.network.api.falai.FalAiApi
import dev.minios.pdaiv1.network.api.horde.HordeRestApi
import dev.minios.pdaiv1.network.api.huggingface.HuggingFaceApi
import dev.minios.pdaiv1.network.api.huggingface.HuggingFaceInferenceApi
import dev.minios.pdaiv1.network.api.huggingface.HuggingFaceInferenceApiImpl
import dev.minios.pdaiv1.network.api.imagecdn.ImageCdnRestApi
import dev.minios.pdaiv1.network.api.imagecdn.ImageCdnRestApiImpl
import dev.minios.pdaiv1.network.api.openai.OpenAiApi
import dev.minios.pdaiv1.network.api.pdai.DonateApi
import dev.minios.pdaiv1.network.api.pdai.DownloadableModelsApi
import dev.minios.pdaiv1.network.api.pdai.DownloadableModelsApiImpl
import dev.minios.pdaiv1.network.api.pdai.HuggingFaceModelsApi
import dev.minios.pdaiv1.network.api.pdai.ReportApi
import dev.minios.pdaiv1.network.api.stabilityai.StabilityAiApi
import dev.minios.pdaiv1.network.api.swarmui.SwarmUiApi
import dev.minios.pdaiv1.network.api.swarmui.SwarmUiApiImpl
import dev.minios.pdaiv1.network.authenticator.RestAuthenticator
import dev.minios.pdaiv1.network.connectivity.ConnectivityMonitor
import dev.minios.pdaiv1.network.error.StabilityAiErrorMapper
import dev.minios.pdaiv1.network.extensions.withBaseUrl
import dev.minios.pdaiv1.network.interceptor.HeaderInterceptor
import dev.minios.pdaiv1.network.interceptor.LoggingInterceptor
import dev.minios.pdaiv1.network.qualifiers.ApiUrlProvider
import dev.minios.pdaiv1.network.qualifiers.HttpInterceptor
import dev.minios.pdaiv1.network.qualifiers.HttpInterceptors
import dev.minios.pdaiv1.network.qualifiers.NetworkInterceptor
import dev.minios.pdaiv1.network.qualifiers.NetworkInterceptors
import dev.minios.pdaiv1.network.qualifiers.RetrofitCallAdapters
import dev.minios.pdaiv1.network.qualifiers.RetrofitConverterFactories
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val HTTP_TIMEOUT = 10L

val networkModule = module {

    single<Gson> {
        GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()
    }

    single { RestAuthenticator(get()) }

    single {
        RetrofitConverterFactories(
            buildList {
                add(GsonConverterFactory.create(get()))
            }
        )
    }

    single {
        RetrofitCallAdapters(
            buildList {
                add(RxJava3CallAdapterFactory.create())
            }
        )
    }

    single {
        HttpInterceptors(
            listOf(
                HttpInterceptor(HeaderInterceptor(get(), get())),
            )
        )
    }

    single {
        NetworkInterceptors(
            listOf(
                NetworkInterceptor(LoggingInterceptor().get()),
            )
        )
    }

    single {
        OkHttpClient
            .Builder()
            .apply {
                get<HttpInterceptors>().interceptors.forEach(::addInterceptor)
                get<NetworkInterceptors>().interceptors.forEach(::addNetworkInterceptor)
                authenticator(get<RestAuthenticator>())
            }
            .connectTimeout(HTTP_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(HTTP_TIMEOUT, TimeUnit.MINUTES)
            .build()
    }

    single<Retrofit.Builder> {
        Retrofit
            .Builder()
            .apply {
                get<RetrofitConverterFactories>().data.forEach(::addConverterFactory)
                get<RetrofitCallAdapters>().data.forEach(::addCallAdapterFactory)
            }
            .client(get())
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().stableDiffusionAutomaticApiUrl)
            .create(Automatic1111RestApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().stableDiffusionAutomaticApiUrl)
            .create(SwarmUiApi.RawApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().hordeApiUrl)
            .create(HordeRestApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().stableDiffusionAppApiUrl)
            .create(DownloadableModelsApi.RawApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().stableDiffusionAppApiUrl)
            .create(HuggingFaceModelsApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().stableDiffusionAppApiUrl)
            .create(DonateApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().stableDiffusionReportApiUrl)
            .create(ReportApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().imageCdnApiUrl)
            .create(ImageCdnRestApi.RawApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().huggingFaceInferenceApiUrl)
            .create(HuggingFaceInferenceApi.RawApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().huggingFaceApiUrl)
            .create(HuggingFaceApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().openAiApiUrl)
            .create(OpenAiApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().stabilityAiApiUrl)
            .create(StabilityAiApi::class.java)
    }

    single {
        get<Retrofit.Builder>()
            .withBaseUrl(get<ApiUrlProvider>().falAiApiUrl)
            .create(FalAiApi::class.java)
    }

    singleOf(::ImageCdnRestApiImpl) bind ImageCdnRestApi::class
    singleOf(::DownloadableModelsApiImpl) bind DownloadableModelsApi::class
    singleOf(::HuggingFaceInferenceApiImpl) bind HuggingFaceInferenceApi::class
    singleOf(::SwarmUiApiImpl) bind SwarmUiApi::class

    factory { params ->
        ConnectivityMonitor(params.get())
    }

    factory { StabilityAiErrorMapper(get()) }
}
