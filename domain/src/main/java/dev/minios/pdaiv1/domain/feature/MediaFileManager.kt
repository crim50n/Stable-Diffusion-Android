package dev.minios.pdaiv1.domain.feature

import dev.minios.pdaiv1.domain.entity.MediaType
import java.io.File

/**
 * Manages media file storage for generated images and videos.
 * All media is stored in context.filesDir/media/ directory.
 */
interface MediaFileManager {

    /**
     * Saves media data to a file and returns the relative path.
     * @param data The raw bytes of the media file
     * @param type The type of media (IMAGE or VIDEO)
     * @return Relative path to the saved file (e.g., "media/abc123.png")
     */
    fun saveMedia(data: ByteArray, type: MediaType): String

    /**
     * Saves media from a URL by downloading it.
     * @param url The URL to download from
     * @param type The type of media
     * @return Relative path to the saved file
     */
    fun saveMediaFromUrl(url: String, type: MediaType): String

    /**
     * Saves input media (for img2img, inpainting) to the input directory.
     * @param data The raw bytes of the media file
     * @param type The type of media
     * @return Relative path to the saved file (e.g., "input/xyz789.png")
     */
    fun saveInputMedia(data: ByteArray, type: MediaType): String

    /**
     * Loads media file contents.
     * @param path Relative path to the file
     * @return File contents as bytes, or null if not found
     */
    fun loadMedia(path: String): ByteArray?

    /**
     * Deletes a media file.
     * @param path Relative path to the file
     * @return true if deleted successfully
     */
    fun deleteMedia(path: String): Boolean

    /**
     * Gets the absolute File reference for a media path.
     * @param path Relative path to the file
     * @return File object
     */
    fun getMediaFile(path: String): File

    /**
     * Migrates base64-encoded data to a file.
     * Used for database migration from base64 storage to file storage.
     * @param base64 Base64-encoded data
     * @param type The type of media
     * @return Relative path to the saved file
     */
    fun migrateBase64ToFile(base64: String, type: MediaType): String

    /**
     * Checks if the path represents file storage (vs legacy base64 or URL).
     * @param path The path or data string to check
     * @return true if this is a file path
     */
    fun isFilePath(path: String): Boolean

    /**
     * Checks if the path represents a video URL (legacy format).
     * @param path The path or data string to check
     * @return true if this is a VIDEO_URL: prefixed string
     */
    fun isVideoUrl(path: String): Boolean

    /**
     * Extracts video URL from legacy VIDEO_URL: format.
     * @param path The VIDEO_URL: prefixed string
     * @return The actual URL, or null if not a video URL
     */
    fun extractVideoUrl(path: String): String?

    companion object {
        const val MEDIA_DIR = "media"
        const val INPUT_DIR = "input"
        const val VIDEO_URL_PREFIX = "VIDEO_URL:"
    }
}
