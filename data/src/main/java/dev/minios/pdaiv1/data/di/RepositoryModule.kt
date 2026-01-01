package dev.minios.pdaiv1.data.di

import android.content.Context
import android.os.PowerManager
import dev.minios.pdaiv1.data.repository.DownloadableModelRepositoryImpl
import dev.minios.pdaiv1.data.repository.EmbeddingsRepositoryImpl
import dev.minios.pdaiv1.data.repository.FalAiEndpointRepositoryImpl
import dev.minios.pdaiv1.data.repository.FalAiGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.ForgeModulesRepositoryImpl
import dev.minios.pdaiv1.data.repository.GenerationResultRepositoryImpl
import dev.minios.pdaiv1.data.repository.HordeGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.HuggingFaceGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.HuggingFaceModelsRepositoryImpl
import dev.minios.pdaiv1.data.repository.LocalDiffusionGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.LorasRepositoryImpl
import dev.minios.pdaiv1.data.repository.MediaPipeGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.OpenAiGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.QnnGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.RandomImageRepositoryImpl
import dev.minios.pdaiv1.data.repository.ReportRepositoryImpl
import dev.minios.pdaiv1.data.repository.ServerConfigurationRepositoryImpl
import dev.minios.pdaiv1.data.repository.StabilityAiCreditsRepositoryImpl
import dev.minios.pdaiv1.data.repository.StabilityAiEnginesRepositoryImpl
import dev.minios.pdaiv1.data.repository.StabilityAiGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.StableDiffusionGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.StableDiffusionHyperNetworksRepositoryImpl
import dev.minios.pdaiv1.data.repository.StableDiffusionModelsRepositoryImpl
import dev.minios.pdaiv1.data.repository.StableDiffusionSamplersRepositoryImpl
import dev.minios.pdaiv1.data.repository.SupportersRepositoryImpl
import dev.minios.pdaiv1.data.repository.SwarmUiGenerationRepositoryImpl
import dev.minios.pdaiv1.data.repository.SwarmUiModelsRepositoryImpl
import dev.minios.pdaiv1.data.repository.TemporaryGenerationResultRepositoryImpl
import dev.minios.pdaiv1.data.repository.WakeLockRepositoryImpl
import dev.minios.pdaiv1.domain.repository.DownloadableModelRepository
import dev.minios.pdaiv1.domain.repository.EmbeddingsRepository
import dev.minios.pdaiv1.domain.repository.FalAiEndpointRepository
import dev.minios.pdaiv1.domain.repository.FalAiGenerationRepository
import dev.minios.pdaiv1.domain.repository.ForgeModulesRepository
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import dev.minios.pdaiv1.domain.repository.HordeGenerationRepository
import dev.minios.pdaiv1.domain.repository.HuggingFaceGenerationRepository
import dev.minios.pdaiv1.domain.repository.HuggingFaceModelsRepository
import dev.minios.pdaiv1.domain.repository.LocalDiffusionGenerationRepository
import dev.minios.pdaiv1.domain.repository.LorasRepository
import dev.minios.pdaiv1.domain.repository.MediaPipeGenerationRepository
import dev.minios.pdaiv1.domain.repository.OpenAiGenerationRepository
import dev.minios.pdaiv1.domain.repository.QnnGenerationRepository
import dev.minios.pdaiv1.domain.repository.RandomImageRepository
import dev.minios.pdaiv1.domain.repository.ReportRepository
import dev.minios.pdaiv1.domain.repository.ServerConfigurationRepository
import dev.minios.pdaiv1.domain.repository.StabilityAiCreditsRepository
import dev.minios.pdaiv1.domain.repository.StabilityAiEnginesRepository
import dev.minios.pdaiv1.domain.repository.StabilityAiGenerationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionGenerationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionHyperNetworksRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionModelsRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionSamplersRepository
import dev.minios.pdaiv1.domain.repository.SupportersRepository
import dev.minios.pdaiv1.domain.repository.SwarmUiGenerationRepository
import dev.minios.pdaiv1.domain.repository.SwarmUiModelsRepository
import dev.minios.pdaiv1.domain.repository.TemporaryGenerationResultRepository
import dev.minios.pdaiv1.domain.repository.WakeLockRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    single<WakeLockRepository> {
        WakeLockRepositoryImpl {
            androidContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        }
    }

    singleOf(::TemporaryGenerationResultRepositoryImpl) bind TemporaryGenerationResultRepository::class
    factoryOf(::LocalDiffusionGenerationRepositoryImpl) bind LocalDiffusionGenerationRepository::class
    factoryOf(::MediaPipeGenerationRepositoryImpl) bind MediaPipeGenerationRepository::class
    factoryOf(::QnnGenerationRepositoryImpl) bind QnnGenerationRepository::class
    factoryOf(::HordeGenerationRepositoryImpl) bind HordeGenerationRepository::class
    factoryOf(::HuggingFaceGenerationRepositoryImpl) bind HuggingFaceGenerationRepository::class
    factoryOf(::OpenAiGenerationRepositoryImpl) bind OpenAiGenerationRepository::class
    factoryOf(::SwarmUiGenerationRepositoryImpl) bind SwarmUiGenerationRepository::class
    factoryOf(::SwarmUiModelsRepositoryImpl) bind SwarmUiModelsRepository::class
    factoryOf(::StabilityAiGenerationRepositoryImpl) bind StabilityAiGenerationRepository::class
    factoryOf(::StabilityAiCreditsRepositoryImpl) bind StabilityAiCreditsRepository::class
    factoryOf(::StabilityAiEnginesRepositoryImpl) bind StabilityAiEnginesRepository::class
    factoryOf(::FalAiGenerationRepositoryImpl) bind FalAiGenerationRepository::class
    factoryOf(::FalAiEndpointRepositoryImpl) bind FalAiEndpointRepository::class
    factoryOf(::StableDiffusionGenerationRepositoryImpl) bind StableDiffusionGenerationRepository::class
    factoryOf(::StableDiffusionModelsRepositoryImpl) bind StableDiffusionModelsRepository::class
    factoryOf(::StableDiffusionSamplersRepositoryImpl) bind StableDiffusionSamplersRepository::class
    factoryOf(::LorasRepositoryImpl) bind LorasRepository::class
    factoryOf(::StableDiffusionHyperNetworksRepositoryImpl) bind StableDiffusionHyperNetworksRepository::class
    factoryOf(::EmbeddingsRepositoryImpl) bind EmbeddingsRepository::class
    factoryOf(::ForgeModulesRepositoryImpl) bind ForgeModulesRepository::class
    factoryOf(::ServerConfigurationRepositoryImpl) bind ServerConfigurationRepository::class
    factoryOf(::GenerationResultRepositoryImpl) bind GenerationResultRepository::class
    factoryOf(::RandomImageRepositoryImpl) bind RandomImageRepository::class
    factoryOf(::DownloadableModelRepositoryImpl) bind DownloadableModelRepository::class
    factoryOf(::HuggingFaceModelsRepositoryImpl) bind HuggingFaceModelsRepository::class
    factoryOf(::SupportersRepositoryImpl) bind SupportersRepository::class
    factoryOf(::ReportRepositoryImpl) bind ReportRepository::class
}
