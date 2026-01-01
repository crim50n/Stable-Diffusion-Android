package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.data.core.CoreGenerationRepository
import dev.minios.pdaiv1.data.mappers.mapLocalDiffusionToAiGenResult
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.mediapipe.MediaPipe
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.MediaPipeGenerationRepository
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

internal class MediaPipeGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    mediaFileManager: MediaFileManager,
    preferenceManager: PreferenceManager,
    private val schedulersProvider: SchedulersProvider,
    private val mediaPipe: MediaPipe,
    private val bitmapToBase64Converter: BitmapToBase64Converter,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    base64ToBitmapConverter = base64ToBitmapConverter,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
    mediaFileManager = mediaFileManager,
), MediaPipeGenerationRepository {

    override fun generateFromText(payload: TextToImagePayload): Single<AiGenerationResult> = mediaPipe
        .process(payload)
        .subscribeOn(schedulersProvider.singleThread.let(Schedulers::from))
        .map(BitmapToBase64Converter::Input)
        .flatMap(bitmapToBase64Converter::invoke)
        .map(BitmapToBase64Converter.Output::base64ImageString)
        .map { base64 -> payload to base64 }
        .map(Pair<TextToImagePayload, String>::mapLocalDiffusionToAiGenResult)
        .flatMap(::insertGenerationResult)
}
