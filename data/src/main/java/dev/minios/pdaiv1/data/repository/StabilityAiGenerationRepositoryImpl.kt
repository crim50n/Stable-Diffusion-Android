package dev.minios.pdaiv1.data.repository

import android.graphics.Bitmap
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashEncoder
import dev.minios.pdaiv1.data.core.CoreGenerationRepository
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.datasource.StabilityAiCreditsDataSource
import dev.minios.pdaiv1.domain.datasource.StabilityAiGenerationDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.StabilityAiGenerationRepository
import io.reactivex.rxjava3.core.Single
import java.io.ByteArrayOutputStream

internal class StabilityAiGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    backgroundWorkObserver: BackgroundWorkObserver,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
    localDataSource: GenerationResultDataSource.Local,
    mediaFileManager: MediaFileManager,
    blurHashEncoder: BlurHashEncoder,
    private val preferenceManager: PreferenceManager,
    private val generationRds: StabilityAiGenerationDataSource.Remote,
    private val creditsRds: StabilityAiCreditsDataSource.Remote,
    private val creditsLds: StabilityAiCreditsDataSource.Local,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    base64ToBitmapConverter = base64ToBitmapConverter,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
    mediaFileManager = mediaFileManager,
    blurHashEncoder = blurHashEncoder,
), StabilityAiGenerationRepository {

    override fun validateApiKey() = generationRds.validateApiKey()

    override fun generateFromText(payload: TextToImagePayload) = generationRds
        .textToImage(preferenceManager.stabilityAiEngineId, payload)
        .flatMap(::insertGenerationResult)
        .flatMap(::refreshCredits)

    override fun generateFromImage(payload: ImageToImagePayload) = payload
        .base64Image
        .let(Base64ToBitmapConverter::Input)
        .let(base64ToBitmapConverter::invoke)
        .map(Base64ToBitmapConverter.Output::bitmap)
        .map { bmp ->
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
        .flatMap { bytes ->
            generationRds.imageToImage(
                engineId = preferenceManager.stabilityAiEngineId,
                payload = payload,
                imageBytes = bytes,
            )
        }
        .flatMap(::insertGenerationResult)
        .flatMap(::refreshCredits)


    private fun refreshCredits(ai: AiGenerationResult) = creditsRds
        .fetch()
        .flatMapCompletable(creditsLds::save)
        .onErrorComplete()
        .andThen(Single.just(ai))
}
