package com.shifthackz.aisdv1.data.core

import com.shifthackz.aisdv1.core.imageprocessing.Base64ToBitmapConverter
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.MediaType
import com.shifthackz.aisdv1.domain.feature.MediaFileManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.reactivex.rxjava3.core.Single

internal abstract class CoreGenerationRepository(
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    private val localDataSource: GenerationResultDataSource.Local,
    private val preferenceManager: PreferenceManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val mediaFileManager: MediaFileManager,
) : CoreMediaStoreRepository(preferenceManager, mediaStoreGateway, base64ToBitmapConverter) {

    protected fun insertGenerationResult(ai: AiGenerationResult): Single<AiGenerationResult> {
        if (backgroundWorkObserver.hasActiveTasks() || preferenceManager.autoSaveAiResults) {
            val converted = ai.saveMediaToFiles()
            return localDataSource
                .insert(converted)
                .flatMap { id -> exportToMediaStore(ai).andThen(Single.just(ai.copy(id))) }
        }
        return Single.just(ai)
    }

    /**
     * Converts base64 data to files before saving to database.
     * This prevents SQLiteBlobTooBigException for large images.
     */
    private fun AiGenerationResult.saveMediaToFiles(): AiGenerationResult {
        var mediaPath = this.mediaPath
        var inputMediaPath = this.inputMediaPath

        // Convert main image base64 to file
        if (image.isNotEmpty() && !mediaFileManager.isFilePath(image) && !mediaFileManager.isVideoUrl(image)) {
            mediaPath = mediaFileManager.migrateBase64ToFile(image, mediaType)
        }

        // Convert input image base64 to file
        if (inputImage.isNotEmpty() && !mediaFileManager.isFilePath(inputImage)) {
            inputMediaPath = mediaFileManager.migrateBase64ToFile(inputImage, MediaType.IMAGE)
        }

        return copy(
            image = "", // Clear base64 from database
            inputImage = "", // Clear base64 from database
            mediaPath = mediaPath,
            inputMediaPath = inputMediaPath,
        )
    }
}
