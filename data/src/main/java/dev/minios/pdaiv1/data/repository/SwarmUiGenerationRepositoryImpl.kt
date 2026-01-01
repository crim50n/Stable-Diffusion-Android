package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.data.core.CoreGenerationRepository
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.datasource.SwarmUiGenerationDataSource
import dev.minios.pdaiv1.domain.datasource.SwarmUiSessionDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.SwarmUiGenerationRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class SwarmUiGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    mediaFileManager: MediaFileManager,
    private val preferenceManager: PreferenceManager,
    private val session: SwarmUiSessionDataSource,
    private val remoteDataSource: SwarmUiGenerationDataSource.Remote,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    base64ToBitmapConverter = base64ToBitmapConverter,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
    mediaFileManager = mediaFileManager,
), SwarmUiGenerationRepository {

    override fun checkApiAvailability(): Completable = session
        .getSessionId()
        .ignoreElement()

    override fun checkApiAvailability(url: String): Completable = session
        .getSessionId(url)
        .ignoreElement()

    override fun generateFromText(payload: TextToImagePayload): Single<AiGenerationResult> = session
        .getSessionId()
        .flatMap { sessionId ->
            remoteDataSource.textToImage(
                sessionId = sessionId,
                model = preferenceManager.swarmUiModel,
                payload = payload,
            )
        }
        .let(session::handleSessionError)
        .flatMap(::insertGenerationResult)

    override fun generateFromImage(payload: ImageToImagePayload): Single<AiGenerationResult> = session
        .getSessionId()
        .flatMap { sessionId ->
            remoteDataSource.imageToImage(
                sessionId = sessionId,
                model = preferenceManager.swarmUiModel,
                payload = payload,
            )
        }
        .let(session::handleSessionError)
        .flatMap(::insertGenerationResult)
}
