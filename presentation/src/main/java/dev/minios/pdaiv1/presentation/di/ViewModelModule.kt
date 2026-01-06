package dev.minios.pdaiv1.presentation.di

import dev.minios.pdaiv1.presentation.activity.AiStableDiffusionViewModel
import dev.minios.pdaiv1.presentation.modal.download.DownloadDialogViewModel
import dev.minios.pdaiv1.presentation.modal.embedding.EmbeddingViewModel
import dev.minios.pdaiv1.presentation.modal.extras.ExtrasViewModel
import dev.minios.pdaiv1.presentation.modal.history.InputHistoryViewModel
import dev.minios.pdaiv1.presentation.modal.tag.EditTagViewModel
import dev.minios.pdaiv1.presentation.model.LaunchSource
import dev.minios.pdaiv1.presentation.screen.debug.DebugMenuViewModel
import dev.minios.pdaiv1.presentation.screen.donate.DonateViewModel
import dev.minios.pdaiv1.presentation.screen.drawer.DrawerViewModel
import dev.minios.pdaiv1.presentation.screen.falai.FalAiGenerationViewModel
import dev.minios.pdaiv1.presentation.screen.gallery.detail.GalleryDetailViewModel
import dev.minios.pdaiv1.presentation.screen.gallery.editor.ImageEditorViewModel
import dev.minios.pdaiv1.presentation.screen.gallery.list.GalleryViewModel
import dev.minios.pdaiv1.presentation.screen.home.HomeNavigationViewModel
import dev.minios.pdaiv1.presentation.screen.img2img.ImageToImageViewModel
import dev.minios.pdaiv1.presentation.screen.inpaint.InPaintViewModel
import dev.minios.pdaiv1.presentation.screen.loader.ConfigurationLoaderViewModel
import dev.minios.pdaiv1.presentation.screen.logger.LoggerViewModel
import dev.minios.pdaiv1.presentation.screen.onboarding.OnBoardingViewModel
import dev.minios.pdaiv1.presentation.screen.report.ReportViewModel
import dev.minios.pdaiv1.presentation.screen.settings.SettingsViewModel
import dev.minios.pdaiv1.presentation.screen.setup.ServerSetupViewModel
import dev.minios.pdaiv1.presentation.screen.splash.SplashViewModel
import dev.minios.pdaiv1.presentation.screen.txt2img.TextToImageViewModel
import dev.minios.pdaiv1.presentation.screen.web.webui.WebUiViewModel
import dev.minios.pdaiv1.presentation.theme.global.AiSdAppThemeViewModel
import dev.minios.pdaiv1.presentation.widget.connectivity.ConnectivityViewModel
import dev.minios.pdaiv1.presentation.widget.engine.EngineSelectionViewModel
import dev.minios.pdaiv1.presentation.widget.work.BackgroundWorkViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::AiStableDiffusionViewModel)
    viewModelOf(::AiSdAppThemeViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::DrawerViewModel)
    viewModelOf(::HomeNavigationViewModel)
    viewModelOf(::ConfigurationLoaderViewModel)
    viewModelOf(::TextToImageViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel {
        GalleryViewModel(
            dispatchersProvider = get(),
            getMediaStoreInfoUseCase = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            deleteAllGalleryUseCase = get(),
            deleteAllUnlikedUseCase = get(),
            deleteGalleryItemsUseCase = get(),
            getGenerationResultPagedUseCase = get(),
            getGalleryPagedIdsUseCase = get(),
            getGalleryItemsUseCase = get(),
            getGalleryItemsRawUseCase = get(),
            getThumbnailInfoUseCase = get(),
            base64ToBitmapConverter = get(),
            thumbnailGenerator = get(),
            galleryExporter = get(),
            schedulersProvider = get(),
            mainRouter = get(),
            drawerRouter = get(),
            mediaStoreGateway = get(),
            mediaFileManager = get(),
            getAllGalleryUseCase = get(),
            galleryItemStateEvent = get(),
            likeItemsUseCase = get(),
            hideItemsUseCase = get(),
        )
    }
    viewModelOf(::ConnectivityViewModel)
    viewModelOf(::InputHistoryViewModel)
    viewModelOf(::DebugMenuViewModel)
    viewModelOf(::ExtrasViewModel)
    viewModelOf(::EmbeddingViewModel)
    viewModelOf(::EditTagViewModel)
    viewModelOf(::InPaintViewModel)
    viewModelOf(::EngineSelectionViewModel)
    viewModelOf(::WebUiViewModel)
    viewModelOf(::DonateViewModel)
    viewModelOf(::BackgroundWorkViewModel)
    viewModelOf(::LoggerViewModel)
    viewModelOf(::DownloadDialogViewModel)
    viewModelOf(::FalAiGenerationViewModel)

    viewModel { parameters ->
        OnBoardingViewModel(
            launchSource = LaunchSource.fromKey(parameters.get()),
            dispatchersProvider = get(),
            mainRouter = get(),
            splashNavigationUseCase = get(),
            preferenceManager = get(),
            schedulersProvider = get(),
            buildInfoProvider = get(),
        )
    }

    viewModel { parameters ->
        val launchSource = LaunchSource.fromKey(parameters.get())
        ServerSetupViewModel(
            launchSource = launchSource,
            dispatchersProvider = get(),
            getConfigurationUseCase = get(),
            getLocalOnnxModelsUseCase = get(),
            getLocalMediaPipeModelsUseCase = get(),
            getLocalQnnModelsUseCase = get(),
            fetchAndGetHuggingFaceModelsUseCase = get(),
            falAiEndpointRepository = get(),
            urlValidator = get(),
            stringValidator = get(),
            filePathValidator = get(),
            setupConnectionInterActor = get(),
            downloadModelUseCase = get(),
            deleteModelUseCase = get(),
            scanCustomModelsUseCase = get(),
            schedulersProvider = get(),
            preferenceManager = get(),
            wakeLockInterActor = get(),
            mainRouter = get(),
            buildInfoProvider = get(),
        )
    }

    viewModel { parameters ->
        GalleryDetailViewModel(
            itemId = parameters.get(),
            onNavigateBackCallback = parameters.getOrNull(),
            dispatchersProvider = get(),
            buildInfoProvider = get(),
            preferenceManager = get(),
            getGenerationResultUseCase = get(),
            getLastResultFromCacheUseCase = get(),
            getGalleryPagedIdsUseCase = get(),
            deleteGalleryItemUseCase = get(),
            toggleImageVisibilityUseCase = get(),
            toggleLikeUseCase = get(),
            galleryDetailBitmapExporter = get(),
            base64ToBitmapConverter = get(),
            schedulersProvider = get(),
            generationFormUpdateEvent = get(),
            galleryItemStateEvent = get(),
            mainRouter = get(),
            mediaStoreGateway = get(),
            backgroundWorkObserver = get(),
        )
    }

    viewModel { parameters ->
        ReportViewModel(
            itemId = parameters.get(),
            sendReportUseCase = get(),
            getGenerationResultUseCase = get(),
            getLastResultFromCacheUseCase = get(),
            base64ToBitmapConverter = get(),
            mainRouter = get(),
            schedulersProvider = get(),
            buildInfoProvider = get(),
        )
    }

    viewModel { parameters ->
        ImageEditorViewModel(
            itemId = parameters.get(),
            dispatchersProvider = get(),
            getGenerationResultUseCase = get(),
            base64ToBitmapConverter = get(),
            mediaStoreGateway = get(),
            schedulersProvider = get(),
            mainRouter = get(),
        )
    }

    viewModel {
        ImageToImageViewModel(
            dispatchersProvider = get(),
            generationFormUpdateEvent = get(),
            getStableDiffusionSamplersUseCase = get(),
            getForgeModulesUseCase = get(),
            observeHordeProcessStatusUseCase = get(),
            observeLocalDiffusionProcessStatusUseCase = get(),
            saveLastResultToCacheUseCase = get(),
            saveGenerationResultUseCase = get(),
            interruptGenerationUseCase = get(),
            drawerRouter = get(),
            dimensionValidator = get(),
            imageToImageUseCase = get(),
            getRandomImageUseCase = get(),
            bitmapToBase64Converter = get(),
            base64ToBitmapConverter = get(),
            preferenceManager = get(),
            schedulersProvider = get(),
            notificationManager = get(),
            wakeLockInterActor = get(),
            inPaintStateProducer = get(),
            mainRouter = get(),
            backgroundTaskManager = get(),
            backgroundWorkObserver = get(),
            buildInfoProvider = get(),
        )
    }
}
