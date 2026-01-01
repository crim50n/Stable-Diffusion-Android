package dev.minios.pdaiv1.data.di

import dev.minios.pdaiv1.core.common.extensions.fixUrlSlashes
import dev.minios.pdaiv1.data.gateway.ServerConnectivityGatewayImpl
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.data.remote.DownloadableModelRemoteDataSource
import dev.minios.pdaiv1.data.remote.FalAiEndpointRemoteDataSource
import dev.minios.pdaiv1.data.remote.FalAiGenerationRemoteDataSource
import dev.minios.pdaiv1.data.remote.HordeGenerationRemoteDataSource
import dev.minios.pdaiv1.data.remote.HordeStatusSource
import dev.minios.pdaiv1.data.remote.HuggingFaceGenerationRemoteDataSource
import dev.minios.pdaiv1.data.remote.HuggingFaceModelsRemoteDataSource
import dev.minios.pdaiv1.data.remote.OpenAiGenerationRemoteDataSource
import dev.minios.pdaiv1.data.remote.RandomImageRemoteDataSource
import dev.minios.pdaiv1.data.remote.ReportRemoteDataSource
import dev.minios.pdaiv1.data.remote.ServerConfigurationRemoteDataSource
import dev.minios.pdaiv1.data.remote.StabilityAiCreditsRemoteDataSource
import dev.minios.pdaiv1.data.remote.StabilityAiEnginesRemoteDataSource
import dev.minios.pdaiv1.data.remote.StabilityAiGenerationRemoteDataSource
import dev.minios.pdaiv1.data.remote.StableDiffusionEmbeddingsRemoteDataSource
import dev.minios.pdaiv1.data.remote.StableDiffusionGenerationRemoteDataSource
import dev.minios.pdaiv1.data.remote.StableDiffusionHyperNetworksRemoteDataSource
import dev.minios.pdaiv1.data.remote.StableDiffusionLorasRemoteDataSource
import dev.minios.pdaiv1.data.remote.StableDiffusionModelsRemoteDataSource
import dev.minios.pdaiv1.data.remote.StableDiffusionSamplersRemoteDataSource
import dev.minios.pdaiv1.data.remote.SupportersRemoteDataSource
import dev.minios.pdaiv1.data.remote.SwarmUiEmbeddingsRemoteDataSource
import dev.minios.pdaiv1.data.remote.SwarmUiGenerationRemoteDataSource
import dev.minios.pdaiv1.data.remote.SwarmUiLorasRemoteDataSource
import dev.minios.pdaiv1.data.remote.SwarmUiModelsRemoteDataSource
import dev.minios.pdaiv1.data.remote.SwarmUiSessionDataSourceImpl
import dev.minios.pdaiv1.domain.datasource.DownloadableModelDataSource
import dev.minios.pdaiv1.domain.datasource.EmbeddingsDataSource
import dev.minios.pdaiv1.domain.datasource.FalAiEndpointDataSource
import dev.minios.pdaiv1.domain.datasource.FalAiGenerationDataSource
import dev.minios.pdaiv1.domain.datasource.HordeGenerationDataSource
import dev.minios.pdaiv1.domain.datasource.HuggingFaceGenerationDataSource
import dev.minios.pdaiv1.domain.datasource.HuggingFaceModelsDataSource
import dev.minios.pdaiv1.domain.datasource.LorasDataSource
import dev.minios.pdaiv1.domain.datasource.OpenAiGenerationDataSource
import dev.minios.pdaiv1.domain.datasource.RandomImageDataSource
import dev.minios.pdaiv1.domain.datasource.ReportDataSource
import dev.minios.pdaiv1.domain.datasource.ServerConfigurationDataSource
import dev.minios.pdaiv1.domain.datasource.StabilityAiCreditsDataSource
import dev.minios.pdaiv1.domain.datasource.StabilityAiEnginesDataSource
import dev.minios.pdaiv1.domain.datasource.StabilityAiGenerationDataSource
import dev.minios.pdaiv1.domain.datasource.StableDiffusionGenerationDataSource
import dev.minios.pdaiv1.domain.datasource.StableDiffusionHyperNetworksDataSource
import dev.minios.pdaiv1.domain.datasource.StableDiffusionModelsDataSource
import dev.minios.pdaiv1.domain.datasource.StableDiffusionSamplersDataSource
import dev.minios.pdaiv1.domain.datasource.SupportersDataSource
import dev.minios.pdaiv1.domain.datasource.SwarmUiGenerationDataSource
import dev.minios.pdaiv1.domain.datasource.SwarmUiModelsDataSource
import dev.minios.pdaiv1.domain.datasource.SwarmUiSessionDataSource
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.gateway.ServerConnectivityGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.network.connectivity.ConnectivityMonitor
import io.reactivex.rxjava3.core.Single
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module

val remoteDataSourceModule = module {
    single {
        ServerUrlProvider { endpoint ->
            val prefs = get<PreferenceManager>()
            val chain = if (prefs.source == ServerSource.SWARM_UI) {
                Single.fromCallable(prefs::swarmUiServerUrl)
            } else {
                Single.fromCallable(prefs::automatic1111ServerUrl)
            }
            chain
                .map(String::fixUrlSlashes)
                .map { baseUrl -> "$baseUrl/$endpoint" }
        }
    }
    singleOf(::HordeStatusSource) bind HordeGenerationDataSource.StatusSource::class
    factoryOf(::HordeGenerationRemoteDataSource) bind HordeGenerationDataSource.Remote::class
    factoryOf(::HuggingFaceGenerationRemoteDataSource) bind HuggingFaceGenerationDataSource.Remote::class
    factoryOf(::OpenAiGenerationRemoteDataSource) bind OpenAiGenerationDataSource.Remote::class
    factoryOf(::SwarmUiSessionDataSourceImpl) bind SwarmUiSessionDataSource::class
    factoryOf(::SwarmUiGenerationRemoteDataSource) bind SwarmUiGenerationDataSource.Remote::class
    factoryOf(::SwarmUiModelsRemoteDataSource) bind SwarmUiModelsDataSource.Remote::class
    factoryOf(::SwarmUiLorasRemoteDataSource) bind LorasDataSource.Remote.SwarmUi::class
    factoryOf(::SwarmUiEmbeddingsRemoteDataSource) bind EmbeddingsDataSource.Remote.SwarmUi::class
    factoryOf(::StableDiffusionGenerationRemoteDataSource) bind StableDiffusionGenerationDataSource.Remote::class
    factoryOf(::StableDiffusionSamplersRemoteDataSource) bind StableDiffusionSamplersDataSource.Remote::class
    factoryOf(::StableDiffusionModelsRemoteDataSource) bind StableDiffusionModelsDataSource.Remote::class
    factoryOf(::StableDiffusionLorasRemoteDataSource) bind LorasDataSource.Remote.Automatic1111::class
    factoryOf(::StableDiffusionHyperNetworksRemoteDataSource) bind StableDiffusionHyperNetworksDataSource.Remote::class
    factoryOf(::StableDiffusionEmbeddingsRemoteDataSource) bind EmbeddingsDataSource.Remote.Automatic1111::class
    factoryOf(::ServerConfigurationRemoteDataSource) bind ServerConfigurationDataSource.Remote::class
    factoryOf(::RandomImageRemoteDataSource) bind RandomImageDataSource.Remote::class
    factoryOf(::DownloadableModelRemoteDataSource) bind DownloadableModelDataSource.Remote::class
    factoryOf(::SupportersRemoteDataSource) bind SupportersDataSource.Remote::class
    factoryOf(::HuggingFaceModelsRemoteDataSource) bind HuggingFaceModelsDataSource.Remote::class
    factoryOf(::StabilityAiGenerationRemoteDataSource) bind StabilityAiGenerationDataSource.Remote::class
    factoryOf(::StabilityAiCreditsRemoteDataSource) bind StabilityAiCreditsDataSource.Remote::class
    factoryOf(::StabilityAiEnginesRemoteDataSource) bind StabilityAiEnginesDataSource.Remote::class
    factoryOf(::FalAiGenerationRemoteDataSource) bind FalAiGenerationDataSource.Remote::class
    factoryOf(::FalAiEndpointRemoteDataSource) bind FalAiEndpointDataSource.Remote::class
    factoryOf(::ReportRemoteDataSource) bind ReportDataSource.Remote::class

    factory<ServerConnectivityGateway> {
        val lambda: () -> Boolean = {
            val prefs = get<PreferenceManager>()
            prefs.source != ServerSource.AUTOMATIC1111 && prefs.source != ServerSource.SWARM_UI
        }
        val monitor = get<ConnectivityMonitor> { parametersOf(lambda) }
        ServerConnectivityGatewayImpl(monitor, get())
    }
}
