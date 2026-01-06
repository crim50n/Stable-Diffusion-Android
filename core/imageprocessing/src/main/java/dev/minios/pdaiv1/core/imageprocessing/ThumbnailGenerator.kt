package dev.minios.pdaiv1.core.imageprocessing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dev.minios.pdaiv1.core.imageprocessing.cache.ImageCacheManager
import dev.minios.pdaiv1.core.imageprocessing.utils.base64ToBitmap
import dev.minios.pdaiv1.core.imageprocessing.utils.base64ToThumbnailBitmap
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import java.io.File

/**
 * Generates thumbnail images from Base64 encoded images or files.
 * Uses ImageCacheManager for caching.
 */
class ThumbnailGenerator(
    private val processingScheduler: Scheduler,
    private val imageCacheManager: ImageCacheManager,
    private val fallbackBitmap: Bitmap,
) {

    /**
     * Generates a thumbnail from a file path.
     * First checks cache, then generates if not found.
     * Uses inSampleSize decoding for memory efficiency.
     */
    fun generateFromFile(
        id: String,
        filePath: String,
        targetSize: Int = ImageCacheManager.THUMBNAIL_SIZE,
    ): Single<Bitmap> = Single
        .defer {
            // Check cache first
            val cached = imageCacheManager.getThumbnail(id)
            if (cached != null) {
                return@defer Single.just(cached)
            }

            // Generate thumbnail from file with subsampled decoding
            Single.fromCallable {
                val file = File(filePath)
                if (!file.exists()) {
                    return@fromCallable fallbackBitmap
                }

                // First decode bounds only
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(filePath, options)

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
                options.inJustDecodeBounds = false

                // Decode with inSampleSize
                val subsampledBitmap = BitmapFactory.decodeFile(filePath, options)
                    ?: return@fromCallable fallbackBitmap

                // Final scale to exact target size if needed
                val thumbnail = createThumbnail(subsampledBitmap, targetSize)

                // Cache the thumbnail
                imageCacheManager.putThumbnail(id, thumbnail)

                // Recycle intermediate bitmap if different from result
                if (subsampledBitmap != thumbnail && !subsampledBitmap.isRecycled) {
                    subsampledBitmap.recycle()
                }

                thumbnail
            }
        }
        .onErrorReturnItem(fallbackBitmap)
        .subscribeOn(processingScheduler)

    /**
     * Generates a thumbnail from base64 string.
     * First checks cache, then generates if not found.
     * Uses inSampleSize decoding for memory efficiency.
     */
    fun generate(
        id: String,
        base64ImageString: String,
        targetSize: Int = ImageCacheManager.THUMBNAIL_SIZE,
    ): Single<Bitmap> = Single
        .defer {
            // Check cache first
            val cached = imageCacheManager.getThumbnail(id)
            if (cached != null) {
                return@defer Single.just(cached)
            }

            // Generate thumbnail with subsampled decoding
            Single.fromCallable {
                // Decode with inSampleSize for memory efficiency
                val subsampledBitmap = base64ToThumbnailBitmap(base64ImageString, targetSize)

                // Final scale to exact target size if needed
                val thumbnail = createThumbnail(subsampledBitmap, targetSize)

                // Cache the thumbnail
                imageCacheManager.putThumbnail(id, thumbnail)

                // Recycle intermediate bitmap if different from result
                if (subsampledBitmap != thumbnail && !subsampledBitmap.isRecycled) {
                    subsampledBitmap.recycle()
                }

                thumbnail
            }
        }
        .onErrorReturnItem(fallbackBitmap)
        .subscribeOn(processingScheduler)

    /**
     * Gets or generates a full-size image from base64.
     */
    fun getFullImage(
        id: String,
        base64ImageString: String,
    ): Single<Bitmap> = Single
        .defer {
            // Check cache first
            val cached = imageCacheManager.getFullImage(id)
            if (cached != null) {
                return@defer Single.just(cached)
            }

            // Load full image
            Single.fromCallable {
                val bitmap = base64ToBitmap(base64ImageString)
                imageCacheManager.putFullImage(id, bitmap)
                bitmap
            }
        }
        .onErrorReturnItem(fallbackBitmap)
        .subscribeOn(processingScheduler)

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun createThumbnail(source: Bitmap, targetSize: Int): Bitmap {
        if (source.width <= targetSize && source.height <= targetSize) {
            return source
        }

        val aspectRatio = source.width.toFloat() / source.height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (aspectRatio > 1) {
            targetWidth = targetSize
            targetHeight = (targetSize / aspectRatio).toInt()
        } else {
            targetHeight = targetSize
            targetWidth = (targetSize * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }
}
