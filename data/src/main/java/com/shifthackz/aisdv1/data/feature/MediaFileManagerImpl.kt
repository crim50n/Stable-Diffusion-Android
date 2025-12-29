package com.shifthackz.aisdv1.data.feature

import android.content.Context
import android.util.Base64
import com.shifthackz.aisdv1.core.common.log.debugLog
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.domain.entity.MediaType
import com.shifthackz.aisdv1.domain.feature.MediaFileManager
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal class MediaFileManagerImpl(
    private val context: Context,
    private val httpClient: OkHttpClient,
) : MediaFileManager {

    private val filesDir: File
        get() = context.filesDir

    override fun saveMedia(data: ByteArray, type: MediaType): String {
        val dir = File(filesDir, MediaFileManager.MEDIA_DIR)
        if (!dir.exists()) dir.mkdirs()

        val fileName = "${UUID.randomUUID()}.${type.extension}"
        val file = File(dir, fileName)

        FileOutputStream(file).use { fos ->
            fos.write(data)
        }

        debugLog("MediaFileManager: Saved media to ${file.absolutePath}")
        return "${MediaFileManager.MEDIA_DIR}/$fileName"
    }

    override fun saveMediaFromUrl(url: String, type: MediaType): String {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to download media: ${response.code}")
        }

        val bytes = response.body?.bytes()
            ?: throw IllegalStateException("Empty response body")

        return saveMedia(bytes, type)
    }

    override fun saveInputMedia(data: ByteArray, type: MediaType): String {
        val dir = File(filesDir, MediaFileManager.INPUT_DIR)
        if (!dir.exists()) dir.mkdirs()

        val fileName = "${UUID.randomUUID()}.${type.extension}"
        val file = File(dir, fileName)

        FileOutputStream(file).use { fos ->
            fos.write(data)
        }

        debugLog("MediaFileManager: Saved input media to ${file.absolutePath}")
        return "${MediaFileManager.INPUT_DIR}/$fileName"
    }

    override fun loadMedia(path: String): ByteArray? {
        return try {
            val file = getMediaFile(path)
            if (file.exists()) {
                file.readBytes()
            } else {
                errorLog("MediaFileManager: File not found: ${file.absolutePath}")
                null
            }
        } catch (e: Exception) {
            errorLog(e)
            null
        }
    }

    override fun deleteMedia(path: String): Boolean {
        return try {
            val file = getMediaFile(path)
            if (file.exists()) {
                val deleted = file.delete()
                debugLog("MediaFileManager: Deleted ${file.absolutePath}: $deleted")
                deleted
            } else {
                true // Already doesn't exist
            }
        } catch (e: Exception) {
            errorLog(e)
            false
        }
    }

    override fun getMediaFile(path: String): File {
        return File(filesDir, path)
    }

    override fun migrateBase64ToFile(base64: String, type: MediaType): String {
        // Handle VIDEO_URL: prefix - for videos, keep the URL format for now
        if (isVideoUrl(base64)) {
            return base64
        }

        // Skip if already a file path
        if (isFilePath(base64)) {
            return base64
        }

        // Decode base64 and save to file
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            saveMedia(bytes, type)
        } catch (e: Exception) {
            errorLog("MediaFileManager: Failed to migrate base64: ${e.message}")
            // Return original on error - don't lose data
            base64
        }
    }

    override fun isFilePath(path: String): Boolean {
        return path.startsWith(MediaFileManager.MEDIA_DIR) ||
                path.startsWith(MediaFileManager.INPUT_DIR)
    }

    override fun isVideoUrl(path: String): Boolean {
        return path.startsWith(MediaFileManager.VIDEO_URL_PREFIX)
    }

    override fun extractVideoUrl(path: String): String? {
        return if (isVideoUrl(path)) {
            path.removePrefix(MediaFileManager.VIDEO_URL_PREFIX)
        } else {
            null
        }
    }
}
