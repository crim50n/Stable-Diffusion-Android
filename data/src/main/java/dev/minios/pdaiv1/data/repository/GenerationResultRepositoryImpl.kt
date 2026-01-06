package dev.minios.pdaiv1.data.repository

import android.util.Base64
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.data.core.CoreMediaStoreRepository
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.MediaType
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class GenerationResultRepositoryImpl(
    preferenceManager: PreferenceManager,
    mediaStoreGateway: MediaStoreGateway,
    base64ToBitmapConverter: Base64ToBitmapConverter,
    private val localDataSource: GenerationResultDataSource.Local,
    private val mediaFileManager: MediaFileManager,
) : CoreMediaStoreRepository(
    preferenceManager,
    mediaStoreGateway,
    base64ToBitmapConverter,
), GenerationResultRepository {

    override fun getAll() = localDataSource.queryAll()
        .map { results -> results.map { it.loadMediaFromFiles() } }

    override fun getAllIds() = localDataSource.queryAllIds()

    override fun getAllIdsWithBlurHash() = localDataSource.queryAllIdsWithBlurHash()

    override fun getThumbnailInfoByIds(idList: List<Long>) = localDataSource.queryThumbnailInfoByIdList(idList)

    override fun getPage(limit: Int, offset: Int) = localDataSource.queryPage(limit, offset)
        .map { results -> results.map { it.loadMediaFromFiles() } }

    override fun getMediaStoreInfo() = getInfo()

    override fun getById(id: Long) = localDataSource.queryById(id)
        .map { it.loadMediaFromFiles() }

    override fun getByIds(idList: List<Long>) = localDataSource.queryByIdList(idList)
        .map { results -> results.map { it.loadMediaFromFiles() } }

    override fun getByIdsRaw(idList: List<Long>) = localDataSource.queryByIdList(idList)

    override fun insert(result: AiGenerationResult): Single<Long> {
        val converted = result.saveMediaToFiles()
        return localDataSource
            .insert(converted)
            .flatMap { id -> exportToMediaStore(result).andThen(Single.just(id)) }
    }

    override fun deleteById(id: Long) = localDataSource.queryById(id)
        .doOnSuccess { result ->
            // Delete media files when deleting from gallery
            if (result.mediaPath.isNotEmpty()) {
                mediaFileManager.deleteMedia(result.mediaPath)
            }
            if (result.inputMediaPath.isNotEmpty()) {
                mediaFileManager.deleteMedia(result.inputMediaPath)
            }
        }
        .flatMapCompletable { localDataSource.deleteById(id) }

    override fun deleteByIdList(idList: List<Long>) = localDataSource.queryByIdList(idList)
        .doOnSuccess { results ->
            // Delete media files when deleting from gallery
            results.forEach { result ->
                if (result.mediaPath.isNotEmpty()) {
                    mediaFileManager.deleteMedia(result.mediaPath)
                }
                if (result.inputMediaPath.isNotEmpty()) {
                    mediaFileManager.deleteMedia(result.inputMediaPath)
                }
            }
        }
        .flatMapCompletable { localDataSource.deleteByIdList(idList) }

    override fun deleteAll() = localDataSource.queryAll()
        .doOnSuccess { results ->
            // Delete all media files
            results.forEach { result ->
                if (result.mediaPath.isNotEmpty()) {
                    mediaFileManager.deleteMedia(result.mediaPath)
                }
                if (result.inputMediaPath.isNotEmpty()) {
                    mediaFileManager.deleteMedia(result.inputMediaPath)
                }
            }
        }
        .flatMapCompletable { localDataSource.deleteAll() }

    override fun deleteAllUnliked(): Completable = localDataSource.deleteAllUnliked()

    override fun toggleVisibility(id: Long): Single<Boolean> = localDataSource
        .queryById(id)
        .map { it.copy(hidden = !it.hidden) }
        .flatMap(localDataSource::insert)
        .flatMap { localDataSource.queryById(id) }
        .map(AiGenerationResult::hidden)

    override fun toggleLike(id: Long): Single<Boolean> = localDataSource
        .queryById(id)
        .map { it.copy(liked = !it.liked) }
        .flatMap(localDataSource::insert)
        .flatMap { localDataSource.queryById(id) }
        .map(AiGenerationResult::liked)

    override fun likeByIds(idList: List<Long>): Completable = localDataSource.likeByIds(idList)

    override fun hideByIds(idList: List<Long>): Completable = localDataSource.hideByIds(idList)

    override fun migrateBase64ToFiles(): Completable = localDataSource.queryAll()
        .flatMapCompletable { results ->
            val needsMigration = results.filter { result ->
                // Needs migration if image has base64 data but mediaPath is empty
                (result.image.isNotEmpty() && !mediaFileManager.isFilePath(result.image) && result.mediaPath.isEmpty()) ||
                (result.inputImage.isNotEmpty() && !mediaFileManager.isFilePath(result.inputImage) && result.inputMediaPath.isEmpty())
            }
            if (needsMigration.isEmpty()) {
                Completable.complete()
            } else {
                Completable.concat(needsMigration.map { result ->
                    val migrated = result.saveMediaToFiles()
                    localDataSource.insert(migrated).ignoreElement()
                })
            }
        }

    /**
     * Converts base64 data to files before saving to database.
     * After this, image/inputImage will be empty, mediaPath/inputMediaPath will contain file paths.
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

    /**
     * Loads media from files into base64 fields for UI consumption.
     */
    private fun AiGenerationResult.loadMediaFromFiles(): AiGenerationResult {
        var loadedImage = image
        var loadedInputImage = inputImage

        // Load main media from file if path is set and image is empty
        if (mediaPath.isNotEmpty() && image.isEmpty()) {
            if (mediaFileManager.isVideoUrl(mediaPath)) {
                // For videos, keep the VIDEO_URL: format
                loadedImage = mediaPath
            } else if (mediaFileManager.isFilePath(mediaPath)) {
                mediaFileManager.loadMedia(mediaPath)?.let { bytes ->
                    loadedImage = Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            }
        }

        // Load input media from file if path is set and inputImage is empty
        if (inputMediaPath.isNotEmpty() && inputImage.isEmpty()) {
            if (mediaFileManager.isFilePath(inputMediaPath)) {
                mediaFileManager.loadMedia(inputMediaPath)?.let { bytes ->
                    loadedInputImage = Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            }
        }

        return copy(
            image = loadedImage,
            inputImage = loadedInputImage,
        )
    }
}
