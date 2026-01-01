package dev.minios.pdaiv1.data.di

import dev.minios.pdaiv1.data.feature.MediaFileManagerImpl
import dev.minios.pdaiv1.data.gateway.DatabaseClearGatewayImpl
import dev.minios.pdaiv1.data.gateway.mediastore.MediaStoreGatewayFactory
import dev.minios.pdaiv1.data.local.DownloadableModelLocalDataSource
import dev.minios.pdaiv1.data.local.EmbeddingsLocalDataSource
import dev.minios.pdaiv1.data.local.FalAiEndpointBuiltInDataSource
import dev.minios.pdaiv1.data.local.FalAiEndpointLocalDataSource
import dev.minios.pdaiv1.data.local.GenerationResultLocalDataSource
import dev.minios.pdaiv1.data.local.HuggingFaceModelsLocalDataSource
import dev.minios.pdaiv1.data.local.LorasLocalDataSource
import dev.minios.pdaiv1.data.local.ServerConfigurationLocalDataSource
import dev.minios.pdaiv1.data.local.StabilityAiCreditsLocalDataSource
import dev.minios.pdaiv1.data.local.StableDiffusionHyperNetworksLocalDataSource
import dev.minios.pdaiv1.data.local.StableDiffusionModelsLocalDataSource
import dev.minios.pdaiv1.data.local.StableDiffusionSamplersLocalDataSource
import dev.minios.pdaiv1.data.local.SupportersLocalDataSource
import dev.minios.pdaiv1.data.local.SwarmUiModelsLocalDataSource
import dev.minios.pdaiv1.domain.datasource.DownloadableModelDataSource
import dev.minios.pdaiv1.domain.datasource.EmbeddingsDataSource
import dev.minios.pdaiv1.domain.datasource.FalAiEndpointDataSource
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.datasource.HuggingFaceModelsDataSource
import dev.minios.pdaiv1.domain.datasource.LorasDataSource
import dev.minios.pdaiv1.domain.datasource.ServerConfigurationDataSource
import dev.minios.pdaiv1.domain.datasource.StabilityAiCreditsDataSource
import dev.minios.pdaiv1.domain.datasource.StableDiffusionHyperNetworksDataSource
import dev.minios.pdaiv1.domain.datasource.StableDiffusionModelsDataSource
import dev.minios.pdaiv1.domain.datasource.StableDiffusionSamplersDataSource
import dev.minios.pdaiv1.domain.datasource.SupportersDataSource
import dev.minios.pdaiv1.domain.datasource.SwarmUiModelsDataSource
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.gateway.DatabaseClearGateway
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val localDataSourceModule = module {
    singleOf(::DatabaseClearGatewayImpl) bind DatabaseClearGateway::class
    single<MediaFileManager> { MediaFileManagerImpl(androidContext(), get()) }
    // !!! Do not use [factoryOf] for StabilityAiCreditsLocalDataSource, it has default constructor
    single<StabilityAiCreditsDataSource.Local> { StabilityAiCreditsLocalDataSource() }
    factoryOf(::StableDiffusionModelsLocalDataSource) bind StableDiffusionModelsDataSource.Local::class
    factoryOf(::StableDiffusionSamplersLocalDataSource) bind StableDiffusionSamplersDataSource.Local::class
    factoryOf(::LorasLocalDataSource) bind LorasDataSource.Local::class
    factoryOf(::StableDiffusionHyperNetworksLocalDataSource) bind StableDiffusionHyperNetworksDataSource.Local::class
    factoryOf(::EmbeddingsLocalDataSource) bind EmbeddingsDataSource.Local::class
    factoryOf(::SwarmUiModelsLocalDataSource) bind SwarmUiModelsDataSource.Local::class
    factoryOf(::ServerConfigurationLocalDataSource) bind ServerConfigurationDataSource.Local::class
    factoryOf(::GenerationResultLocalDataSource) bind GenerationResultDataSource.Local::class
    factoryOf(::DownloadableModelLocalDataSource) bind DownloadableModelDataSource.Local::class
    factoryOf(::HuggingFaceModelsLocalDataSource) bind HuggingFaceModelsDataSource.Local::class
    factoryOf(::SupportersLocalDataSource) bind SupportersDataSource.Local::class
    factoryOf(::FalAiEndpointLocalDataSource) bind FalAiEndpointDataSource.Local::class
    factoryOf(::FalAiEndpointBuiltInDataSource) bind FalAiEndpointDataSource.BuiltIn::class
    factory { MediaStoreGatewayFactory(androidContext(), get()).invoke() }
}
