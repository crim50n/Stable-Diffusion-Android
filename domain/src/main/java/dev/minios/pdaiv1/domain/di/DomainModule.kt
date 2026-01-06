package dev.minios.pdaiv1.domain.di

import dev.minios.pdaiv1.domain.interactor.settings.SetupConnectionInterActor
import dev.minios.pdaiv1.domain.interactor.settings.SetupConnectionInterActorImpl
import dev.minios.pdaiv1.domain.interactor.wakelock.WakeLockInterActor
import dev.minios.pdaiv1.domain.interactor.wakelock.WakeLockInterActorImpl
import dev.minios.pdaiv1.domain.usecase.caching.ClearAppCacheUseCase
import dev.minios.pdaiv1.domain.usecase.caching.ClearAppCacheUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.caching.DataPreLoaderUseCase
import dev.minios.pdaiv1.domain.usecase.caching.DataPreLoaderUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import dev.minios.pdaiv1.domain.usecase.caching.GetLastResultFromCacheUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import dev.minios.pdaiv1.domain.usecase.caching.SaveLastResultToCacheUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.ObserveSeverConnectivityUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.ObserveSeverConnectivityUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.PingStableDiffusionServiceUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.PingStableDiffusionServiceUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.TestConnectivityUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.TestConnectivityUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.TestHordeApiKeyUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.TestHordeApiKeyUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.TestHuggingFaceApiKeyUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.TestHuggingFaceApiKeyUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.TestOpenAiApiKeyUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.TestOpenAiApiKeyUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.TestFalAiApiKeyUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.TestFalAiApiKeyUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.TestStabilityAiApiKeyUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.TestStabilityAiApiKeyUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.connectivity.TestSwarmUiConnectivityUseCase
import dev.minios.pdaiv1.domain.usecase.connectivity.TestSwarmUiConnectivityUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.debug.DebugInsertBadBase64UseCase
import dev.minios.pdaiv1.domain.usecase.debug.DebugInsertBadBase64UseCaseImpl
import dev.minios.pdaiv1.domain.usecase.donate.FetchAndGetSupportersUseCase
import dev.minios.pdaiv1.domain.usecase.donate.FetchAndGetSupportersUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.downloadable.DeleteModelUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.DeleteModelUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.downloadable.DownloadModelUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.DownloadModelUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalModelUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalModelUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalQnnModelsUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.GetLocalQnnModelsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.downloadable.ScanCustomModelsUseCase
import dev.minios.pdaiv1.domain.usecase.downloadable.ScanCustomModelsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.forgemodule.GetForgeModulesUseCase
import dev.minios.pdaiv1.domain.usecase.forgemodule.GetForgeModulesUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteAllGalleryUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteAllGalleryUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteGalleryItemUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteGalleryItemUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteGalleryItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteGalleryItemsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.GetAllGalleryUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetAllGalleryUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryItemsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryItemsRawUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryItemsRawUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryPagedIdsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryPagedIdsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.GetMediaStoreInfoUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetMediaStoreInfoUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.GetThumbnailInfoUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetThumbnailInfoUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.ToggleImageVisibilityUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.ToggleImageVisibilityUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.ToggleLikeUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.ToggleLikeUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteAllUnlikedUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteAllUnlikedUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.LikeItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.LikeItemsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.gallery.HideItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.HideItemsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.FalAiGenerationUseCase
import dev.minios.pdaiv1.domain.usecase.generation.FalAiGenerationUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultUseCase
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.GetRandomImageUseCase
import dev.minios.pdaiv1.domain.usecase.generation.GetRandomImageUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.ImageToImageUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ImageToImageUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.InterruptGenerationUseCase
import dev.minios.pdaiv1.domain.usecase.generation.InterruptGenerationUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveHordeProcessStatusUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.SaveGenerationResultUseCase
import dev.minios.pdaiv1.domain.usecase.generation.SaveGenerationResultUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.generation.TextToImageUseCase
import dev.minios.pdaiv1.domain.usecase.generation.TextToImageUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.huggingface.FetchAndGetHuggingFaceModelsUseCase
import dev.minios.pdaiv1.domain.usecase.huggingface.FetchAndGetHuggingFaceModelsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.report.SendReportUseCase
import dev.minios.pdaiv1.domain.usecase.report.SendReportUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.sdembedding.FetchAndGetEmbeddingsUseCase
import dev.minios.pdaiv1.domain.usecase.sdembedding.FetchAndGetEmbeddingsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.sdhypernet.FetchAndGetHyperNetworksUseCase
import dev.minios.pdaiv1.domain.usecase.sdhypernet.FetchAndGetHyperNetworksUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.sdlora.FetchAndGetLorasUseCase
import dev.minios.pdaiv1.domain.usecase.sdlora.FetchAndGetLorasUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import dev.minios.pdaiv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import dev.minios.pdaiv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import dev.minios.pdaiv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToA1111UseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToA1111UseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToHordeUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToHordeUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToHuggingFaceUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToHuggingFaceUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToLocalDiffusionUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToLocalDiffusionUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToMediaPipeUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToMediaPipeUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToQnnUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToQnnUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToOpenAiUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToOpenAiUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToFalAiUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToFalAiUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToStabilityAiUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToStabilityAiUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToSwarmUiUseCase
import dev.minios.pdaiv1.domain.usecase.settings.ConnectToSwarmUiUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.GetConfigurationUseCase
import dev.minios.pdaiv1.domain.usecase.settings.GetConfigurationUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.settings.SetServerConfigurationUseCase
import dev.minios.pdaiv1.domain.usecase.settings.SetServerConfigurationUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.splash.SplashNavigationUseCase
import dev.minios.pdaiv1.domain.usecase.splash.SplashNavigationUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCase
import dev.minios.pdaiv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.stabilityai.ObserveStabilityAiCreditsUseCase
import dev.minios.pdaiv1.domain.usecase.stabilityai.ObserveStabilityAiCreditsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCase
import dev.minios.pdaiv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.wakelock.AcquireWakelockUseCase
import dev.minios.pdaiv1.domain.usecase.wakelock.AcquireWakelockUseCaseImpl
import dev.minios.pdaiv1.domain.usecase.wakelock.ReleaseWakeLockUseCase
import dev.minios.pdaiv1.domain.usecase.wakelock.ReleaseWakeLockUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val useCasesModule = module {
    factoryOf(::TextToImageUseCaseImpl) bind TextToImageUseCase::class
    factoryOf(::ImageToImageUseCaseImpl) bind ImageToImageUseCase::class
    factoryOf(::FalAiGenerationUseCaseImpl) bind FalAiGenerationUseCase::class
    factoryOf(::PingStableDiffusionServiceUseCaseImpl) bind PingStableDiffusionServiceUseCase::class
    factoryOf(::ClearAppCacheUseCaseImpl) bind ClearAppCacheUseCase::class
    factoryOf(::DataPreLoaderUseCaseImpl) bind DataPreLoaderUseCase::class
    factoryOf(::FetchAndGetSwarmUiModelsUseCaseImpl) bind FetchAndGetSwarmUiModelsUseCase::class
    factoryOf(::GetStableDiffusionModelsUseCaseImpl) bind GetStableDiffusionModelsUseCase::class
    factoryOf(::SelectStableDiffusionModelUseCaseImpl) bind SelectStableDiffusionModelUseCase::class
    factoryOf(::GetGenerationResultPagedUseCaseImpl) bind GetGenerationResultPagedUseCase::class
    factoryOf(::GetAllGalleryUseCaseImpl) bind GetAllGalleryUseCase::class
    factoryOf(::GetGalleryItemsUseCaseImpl) bind GetGalleryItemsUseCase::class
    factoryOf(::GetGalleryItemsRawUseCaseImpl) bind GetGalleryItemsRawUseCase::class
    factoryOf(::GetGalleryPagedIdsUseCaseImpl) bind GetGalleryPagedIdsUseCase::class
    factoryOf(::GetThumbnailInfoUseCaseImpl) bind GetThumbnailInfoUseCase::class
    factoryOf(::GetGenerationResultUseCaseImpl) bind GetGenerationResultUseCase::class
    factoryOf(::DeleteGalleryItemUseCaseImpl) bind DeleteGalleryItemUseCase::class
    factoryOf(::DeleteGalleryItemsUseCaseImpl) bind DeleteGalleryItemsUseCase::class
    factoryOf(::DeleteAllGalleryUseCaseImpl) bind DeleteAllGalleryUseCase::class
    factoryOf(::GetStableDiffusionSamplersUseCaseImpl) bind GetStableDiffusionSamplersUseCase::class
    factoryOf(::FetchAndGetLorasUseCaseImpl) bind FetchAndGetLorasUseCase::class
    factoryOf(::FetchAndGetHyperNetworksUseCaseImpl) bind FetchAndGetHyperNetworksUseCase::class
    factoryOf(::FetchAndGetEmbeddingsUseCaseImpl) bind FetchAndGetEmbeddingsUseCase::class
    factoryOf(::GetForgeModulesUseCaseImpl) bind GetForgeModulesUseCase::class
    factoryOf(::SplashNavigationUseCaseImpl) bind SplashNavigationUseCase::class
    factoryOf(::GetConfigurationUseCaseImpl) bind GetConfigurationUseCase::class
    factoryOf(::SetServerConfigurationUseCaseImpl) bind SetServerConfigurationUseCase::class
    factoryOf(::TestConnectivityUseCaseImpl) bind TestConnectivityUseCase::class
    factoryOf(::TestHordeApiKeyUseCaseImpl) bind TestHordeApiKeyUseCase::class
    factoryOf(::TestHuggingFaceApiKeyUseCaseImpl) bind TestHuggingFaceApiKeyUseCase::class
    factoryOf(::TestOpenAiApiKeyUseCaseImpl) bind TestOpenAiApiKeyUseCase::class
    factoryOf(::TestStabilityAiApiKeyUseCaseImpl) bind TestStabilityAiApiKeyUseCase::class
    factoryOf(::TestFalAiApiKeyUseCaseImpl) bind TestFalAiApiKeyUseCase::class
    factoryOf(::TestSwarmUiConnectivityUseCaseImpl) bind TestSwarmUiConnectivityUseCase::class
    factoryOf(::SaveGenerationResultUseCaseImpl) bind SaveGenerationResultUseCase::class
    factoryOf(::ObserveSeverConnectivityUseCaseImpl) bind ObserveSeverConnectivityUseCase::class
    factoryOf(::ObserveHordeProcessStatusUseCaseImpl) bind ObserveHordeProcessStatusUseCase::class
    factoryOf(::GetMediaStoreInfoUseCaseImpl) bind GetMediaStoreInfoUseCase::class
    factoryOf(::ToggleImageVisibilityUseCaseImpl) bind ToggleImageVisibilityUseCase::class
    factoryOf(::ToggleLikeUseCaseImpl) bind ToggleLikeUseCase::class
    factoryOf(::DeleteAllUnlikedUseCaseImpl) bind DeleteAllUnlikedUseCase::class
    factoryOf(::LikeItemsUseCaseImpl) bind LikeItemsUseCase::class
    factoryOf(::HideItemsUseCaseImpl) bind HideItemsUseCase::class
    factoryOf(::GetRandomImageUseCaseImpl) bind GetRandomImageUseCase::class
    factoryOf(::SaveLastResultToCacheUseCaseImpl) bind SaveLastResultToCacheUseCase::class
    factoryOf(::GetLastResultFromCacheUseCaseImpl) bind GetLastResultFromCacheUseCase::class
    factoryOf(::ObserveLocalDiffusionProcessStatusUseCaseImpl) bind ObserveLocalDiffusionProcessStatusUseCase::class
    factoryOf(::GetLocalOnnxModelsUseCaseImpl) bind GetLocalOnnxModelsUseCase::class
    factoryOf(::GetLocalMediaPipeModelsUseCaseImpl) bind GetLocalMediaPipeModelsUseCase::class
    factoryOf(::GetLocalQnnModelsUseCaseImpl) bind GetLocalQnnModelsUseCase::class
    factoryOf(::ScanCustomModelsUseCaseImpl) bind ScanCustomModelsUseCase::class
    factoryOf(::DownloadModelUseCaseImpl) bind DownloadModelUseCase::class
    factoryOf(::ObserveLocalOnnxModelsUseCaseImpl) bind ObserveLocalOnnxModelsUseCase::class
    factoryOf(::DeleteModelUseCaseImpl) bind DeleteModelUseCase::class
    factoryOf(::AcquireWakelockUseCaseImpl) bind AcquireWakelockUseCase::class
    factoryOf(::ReleaseWakeLockUseCaseImpl) bind ReleaseWakeLockUseCase::class
    factoryOf(::InterruptGenerationUseCaseImpl) bind InterruptGenerationUseCase::class
    factoryOf(::ConnectToHordeUseCaseImpl) bind ConnectToHordeUseCase::class
    factoryOf(::ConnectToLocalDiffusionUseCaseImpl) bind ConnectToLocalDiffusionUseCase::class
    factoryOf(::ConnectToMediaPipeUseCaseImpl) bind ConnectToMediaPipeUseCase::class
    factoryOf(::ConnectToQnnUseCaseImpl) bind ConnectToQnnUseCase::class
    factoryOf(::ConnectToA1111UseCaseImpl) bind ConnectToA1111UseCase::class
    factoryOf(::ConnectToSwarmUiUseCaseImpl) bind ConnectToSwarmUiUseCase::class
    factoryOf(::ConnectToHuggingFaceUseCaseImpl) bind ConnectToHuggingFaceUseCase::class
    factoryOf(::ConnectToOpenAiUseCaseImpl) bind ConnectToOpenAiUseCase::class
    factoryOf(::ConnectToStabilityAiUseCaseImpl) bind ConnectToStabilityAiUseCase::class
    factoryOf(::ConnectToFalAiUseCaseImpl) bind ConnectToFalAiUseCase::class
    factoryOf(::FetchAndGetHuggingFaceModelsUseCaseImpl) bind FetchAndGetHuggingFaceModelsUseCase::class
    factoryOf(::ObserveStabilityAiCreditsUseCaseImpl) bind ObserveStabilityAiCreditsUseCase::class
    factoryOf(::FetchAndGetStabilityAiEnginesUseCaseImpl) bind FetchAndGetStabilityAiEnginesUseCase::class
    factoryOf(::FetchAndGetSupportersUseCaseImpl) bind FetchAndGetSupportersUseCase::class
    factoryOf(::SendReportUseCaseImpl) bind SendReportUseCase::class
    factoryOf(::GetLocalModelUseCaseImpl) bind GetLocalModelUseCase::class
}

internal val interActorsModule = module {
    factoryOf(::WakeLockInterActorImpl) bind WakeLockInterActor::class
    factoryOf(::SetupConnectionInterActorImpl) bind SetupConnectionInterActor::class
}

internal val debugModule = module {
    factoryOf(::DebugInsertBadBase64UseCaseImpl) bind DebugInsertBadBase64UseCase::class
}

val domainModule = (useCasesModule + interActorsModule + debugModule).toTypedArray()
