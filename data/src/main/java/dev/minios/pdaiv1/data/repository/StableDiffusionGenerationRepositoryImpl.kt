package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.data.core.CoreGenerationRepository
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.datasource.StableDiffusionGenerationDataSource
import dev.minios.pdaiv1.domain.demo.ImageToImageDemo
import dev.minios.pdaiv1.domain.demo.TextToImageDemo
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.StableDiffusionGenerationRepository
import io.reactivex.rxjava3.core.Single

internal class StableDiffusionGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    backgroundWorkObserver: BackgroundWorkObserver,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    localDataSource: GenerationResultDataSource.Local,
    mediaFileManager: MediaFileManager,
    private val remoteDataSource: StableDiffusionGenerationDataSource.Remote,
    private val preferenceManager: PreferenceManager,
    private val textToImageDemo: TextToImageDemo,
    private val imageToImageDemo: ImageToImageDemo,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    base64ToBitmapConverter = base64ToBitmapConverter,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
    mediaFileManager = mediaFileManager,
), StableDiffusionGenerationRepository {

    override fun checkApiAvailability() = remoteDataSource.checkAvailability()

    override fun checkApiAvailability(url: String) = remoteDataSource.checkAvailability(url)

    override fun generateFromText(payload: TextToImagePayload): Single<AiGenerationResult> {
        val chain =
            if (preferenceManager.demoMode) textToImageDemo.getDemoBase64(payload)
            else remoteDataSource.textToImage(payload)

        return chain.flatMap(::insertGenerationResult)
    }

    override fun generateFromImage(payload: ImageToImagePayload): Single<AiGenerationResult> {
        val chain =
            if (preferenceManager.demoMode) imageToImageDemo.getDemoBase64(payload)
            else remoteDataSource.imageToImage(payload)

        return chain.flatMap(::insertGenerationResult)
    }

    override fun interruptGeneration() = remoteDataSource.interruptGeneration()
}
