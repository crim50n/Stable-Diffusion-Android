package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashEncoder
import dev.minios.pdaiv1.data.core.CoreGenerationRepository
import dev.minios.pdaiv1.data.mappers.QnnGenerationData
import dev.minios.pdaiv1.data.mappers.mapQnnResultToAiGenResult
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.qnn.LocalQnn
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.QnnGenerationRepository
import io.reactivex.rxjava3.core.Completable

internal class QnnGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    mediaFileManager: MediaFileManager,
    blurHashEncoder: BlurHashEncoder,
    private val preferenceManager: PreferenceManager,
    private val localQnn: LocalQnn,
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
), QnnGenerationRepository {

    override fun observeStatus() = localQnn.observeStatus()

    override fun generateFromText(payload: TextToImagePayload) = localQnn
        .processTextToImage(payload)
        .subscribeOn(schedulersProvider.io)
        .flatMap { qnnResult ->
            bitmapToBase64Converter(BitmapToBase64Converter.Input(qnnResult.bitmap))
                .map { output ->
                    QnnGenerationData(
                        payload = payload,
                        base64 = output.base64ImageString,
                        seed = qnnResult.seed,
                        width = qnnResult.width,
                        height = qnnResult.height,
                    )
                }
        }
        .map(QnnGenerationData::mapQnnResultToAiGenResult)
        .flatMap(::insertGenerationResult)

    override fun generateFromImage(payload: ImageToImagePayload) = localQnn
        .processImageToImage(payload)
        .subscribeOn(schedulersProvider.io)
        .flatMap { qnnResult ->
            bitmapToBase64Converter(BitmapToBase64Converter.Input(qnnResult.bitmap))
                .map { output ->
                    QnnGenerationData(
                        payload = payload,
                        base64 = output.base64ImageString,
                        seed = qnnResult.seed,
                        width = qnnResult.width,
                        height = qnnResult.height,
                    )
                }
        }
        .map(QnnGenerationData::mapQnnResultToAiGenResult)
        .flatMap(::insertGenerationResult)

    override fun interruptGeneration(): Completable = localQnn.interrupt()
}
