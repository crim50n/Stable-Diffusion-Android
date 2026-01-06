package dev.minios.pdaiv1.core.imageprocessing.blurhash

import android.graphics.Bitmap
import com.vanniktech.blurhash.BlurHash
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single

/**
 * Decodes BlurHash strings to Bitmap images for placeholders.
 */
class BlurHashDecoder(
    private val processingScheduler: Scheduler,
    private val fallbackBitmap: Bitmap,
) {

    /**
     * Decodes a BlurHash string to a Bitmap synchronously.
     * Use for inline Composable rendering.
     */
    fun decodeSync(
        hash: String,
        width: Int = DEFAULT_SIZE,
        height: Int = DEFAULT_SIZE,
    ): Bitmap? {
        if (hash.isBlank()) return null
        return try {
            BlurHash.decode(hash, width, height)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodes a BlurHash string to a Bitmap.
     * @param hash The BlurHash string to decode
     * @param width Target width of the decoded bitmap
     * @param height Target height of the decoded bitmap
     * @return Single emitting the decoded Bitmap
     */
    fun decode(
        hash: String,
        width: Int = DEFAULT_SIZE,
        height: Int = DEFAULT_SIZE,
    ): Single<Bitmap> = Single
        .fromCallable {
            if (hash.isBlank()) {
                return@fromCallable fallbackBitmap
            }
            BlurHash.decode(hash, width, height) ?: fallbackBitmap
        }
        .onErrorReturnItem(fallbackBitmap)
        .subscribeOn(processingScheduler)

    companion object {
        const val DEFAULT_SIZE = 32 // Small size for blur placeholder

        /**
         * Static decode for simple use cases without dependency injection.
         */
        fun decodeStatic(hash: String, width: Int = DEFAULT_SIZE, height: Int = DEFAULT_SIZE): Bitmap? {
            if (hash.isBlank()) return null
            return try {
                BlurHash.decode(hash, width, height)
            } catch (e: Exception) {
                null
            }
        }
    }
}
