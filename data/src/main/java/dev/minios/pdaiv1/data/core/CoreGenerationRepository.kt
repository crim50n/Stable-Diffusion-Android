package dev.minios.pdaiv1.data.core

import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashEncoder
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.MediaType
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.reactivex.rxjava3.core.Single

internal abstract class CoreGenerationRepository(
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    private val localDataSource: GenerationResultDataSource.Local,
    private val preferenceManager: PreferenceManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val mediaFileManager: MediaFileManager,
    private val blurHashEncoder: BlurHashEncoder,
) : CoreMediaStoreRepository(preferenceManager, mediaStoreGateway, base64ToBitmapConverter) {

    protected fun insertGenerationResult(ai: AiGenerationResult): Single<AiGenerationResult> {
        if (backgroundWorkObserver.hasActiveTasks() || preferenceManager.autoSaveAiResults) {
            val converted = ai.saveMediaToFiles()
            return localDataSource
                .insert(converted)
                .flatMap { id -> exportToMediaStore(ai).andThen(Single.just(ai.copy(id))) }
                .doOnSuccess { backgroundWorkObserver.postNewImageSignal() }
        }
        return Single.just(ai)
    }

    /**
     * Converts base64 data to files before saving to database.
     * This prevents SQLiteBlobTooBigException for large images.
     * Also generates BlurHash for gallery placeholders.
     */
    private fun AiGenerationResult.saveMediaToFiles(): AiGenerationResult {
        var mediaPath = this.mediaPath
        var inputMediaPath = this.inputMediaPath
        var blurHash = this.blurHash

        // Convert main image base64 to file and generate BlurHash
        if (image.isNotEmpty() && !mediaFileManager.isFilePath(image) && !mediaFileManager.isVideoUrl(image)) {
            mediaPath = mediaFileManager.migrateBase64ToFile(image, mediaType)
            // Generate BlurHash for gallery placeholder
            if (blurHash.isEmpty()) {
                blurHash = generateBlurHash(image)
            }
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
            blurHash = blurHash,
        )
    }

    /**
     * Generates BlurHash from base64 image string.
     * Returns empty string on failure.
     */
    private fun generateBlurHash(base64Image: String): String {
        return try {
            val bytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                // Scale down for faster encoding
                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 32, 32, true)
                val hash = blurHashEncoder.encodeSync(scaledBitmap)
                if (scaledBitmap != bitmap) scaledBitmap.recycle()
                bitmap.recycle()
                hash
            } else ""
        } catch (e: Exception) {
            ""
        }
    }
}
