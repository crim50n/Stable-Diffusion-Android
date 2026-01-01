package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.core.common.schedulers.SchedulersProvider
import com.shifthackz.aisdv1.core.imageprocessing.Base64ToBitmapConverter
import com.shifthackz.aisdv1.core.imageprocessing.BitmapToBase64Converter
import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.data.mappers.QnnGenerationData
import com.shifthackz.aisdv1.data.mappers.mapQnnResultToAiGenResult
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.MediaFileManager
import com.shifthackz.aisdv1.domain.feature.qnn.LocalQnn
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.QnnGenerationRepository
import io.reactivex.rxjava3.core.Completable

internal class QnnGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    mediaFileManager: MediaFileManager,
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
