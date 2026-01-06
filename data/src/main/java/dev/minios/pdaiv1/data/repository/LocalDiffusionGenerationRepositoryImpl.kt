package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashEncoder
import dev.minios.pdaiv1.data.core.CoreGenerationRepository
import dev.minios.pdaiv1.data.mappers.mapLocalDiffusionToAiGenResult
import dev.minios.pdaiv1.domain.datasource.DownloadableModelDataSource
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.diffusion.LocalDiffusion
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.LocalDiffusionGenerationRepository
import io.reactivex.rxjava3.core.Single

internal class LocalDiffusionGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    mediaFileManager: MediaFileManager,
    blurHashEncoder: BlurHashEncoder,
    private val preferenceManager: PreferenceManager,
    private val localDiffusion: LocalDiffusion,
    private val downloadableLocalDataSource: DownloadableModelDataSource.Local,
    private val bitmapToBase64Converter: BitmapToBase64Converter,
    private val schedulersProvider: SchedulersProvider,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    base64ToBitmapConverter = base64ToBitmapConverter,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
    mediaFileManager = mediaFileManager,
    blurHashEncoder = blurHashEncoder,
), LocalDiffusionGenerationRepository {

    override fun observeStatus() = localDiffusion.observeStatus()

    override fun generateFromText(payload: TextToImagePayload) = downloadableLocalDataSource
        .getSelectedOnnx()
        .flatMap { model ->
            if (model.downloaded) generate(payload)
            else Single.error(IllegalStateException("Model not downloaded."))
        }

    override fun interruptGeneration() = localDiffusion.interrupt()

    private fun generate(payload: TextToImagePayload) = localDiffusion
        .process(payload)
        .subscribeOn(schedulersProvider.byToken(preferenceManager.localOnnxSchedulerThread))
        .map(BitmapToBase64Converter::Input)
        .flatMap(bitmapToBase64Converter::invoke)
        .map(BitmapToBase64Converter.Output::base64ImageString)
        .map { base64 -> payload to base64 }
        .map(Pair<TextToImagePayload, String>::mapLocalDiffusionToAiGenResult)
        .flatMap(::insertGenerationResult)
}
