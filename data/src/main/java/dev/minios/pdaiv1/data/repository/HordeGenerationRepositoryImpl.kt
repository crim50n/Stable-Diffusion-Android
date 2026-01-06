package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashEncoder
import dev.minios.pdaiv1.data.core.CoreGenerationRepository
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.datasource.HordeGenerationDataSource
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.HordeGenerationRepository

internal class HordeGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    localDataSource: GenerationResultDataSource.Local,
    preferenceManager: PreferenceManager,
    backgroundWorkObserver: BackgroundWorkObserver,
    mediaFileManager: MediaFileManager,
    blurHashEncoder: BlurHashEncoder,
    private val remoteDataSource: HordeGenerationDataSource.Remote,
    private val statusSource: HordeGenerationDataSource.StatusSource,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    base64ToBitmapConverter = base64ToBitmapConverter,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
    mediaFileManager = mediaFileManager,
    blurHashEncoder = blurHashEncoder,
), HordeGenerationRepository {

    override fun observeStatus() = statusSource.observe()

    override fun validateApiKey() = remoteDataSource.validateApiKey()

    override fun generateFromText(payload: TextToImagePayload) = remoteDataSource
        .textToImage(payload)
        .flatMap(::insertGenerationResult)

    override fun generateFromImage(payload: ImageToImagePayload) = remoteDataSource
        .imageToImage(payload)
        .flatMap(::insertGenerationResult)

    override fun interruptGeneration() = remoteDataSource.interruptGeneration()
}
