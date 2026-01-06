package dev.minios.pdaiv1.core.imageprocessing.cache

import android.graphics.Bitmap
import android.util.LruCache

/**
 * Manages dual-layer image caching for gallery:
 * - Thumbnail cache: stores small preview images (256x256)
 * - Full image cache: stores full resolution images for viewing
 */
class ImageCacheManager(
    thumbnailCacheSize: Int = DEFAULT_THUMBNAIL_CACHE_SIZE,
    fullImageCacheSize: Int = DEFAULT_FULL_IMAGE_CACHE_SIZE,
) {

    private val thumbnailCache = object : LruCache<String, Bitmap>(thumbnailCacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024 // Size in KB
        }
    }

    private val fullImageCache = object : LruCache<String, Bitmap>(fullImageCacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024 // Size in KB
        }
    }

    fun getThumbnail(id: String): Bitmap? = thumbnailCache.get(id)

    fun putThumbnail(id: String, bitmap: Bitmap) {
        thumbnailCache.put(id, bitmap)
    }

    fun getFullImage(id: String): Bitmap? = fullImageCache.get(id)

    fun putFullImage(id: String, bitmap: Bitmap) {
        fullImageCache.put(id, bitmap)
    }

    fun removeThumbnail(id: String) {
        thumbnailCache.remove(id)
    }

    fun removeFullImage(id: String) {
        fullImageCache.remove(id)
    }

    fun clearThumbnails() {
        thumbnailCache.evictAll()
    }

    fun clearFullImages() {
        fullImageCache.evictAll()
    }

    fun clear() {
        clearThumbnails()
        clearFullImages()
    }

    companion object {
        const val THUMBNAIL_SIZE = 256 // px

        // Cache size in KB (about 2500 thumbnails of ~50KB each = ~125MB)
        // Increased for smoother gallery scrolling like Immich
        private const val DEFAULT_THUMBNAIL_CACHE_SIZE = 125_000

        // Cache size in KB (about 10 full images of ~5MB each = ~50MB)
        private const val DEFAULT_FULL_IMAGE_CACHE_SIZE = 50_000
    }
}
