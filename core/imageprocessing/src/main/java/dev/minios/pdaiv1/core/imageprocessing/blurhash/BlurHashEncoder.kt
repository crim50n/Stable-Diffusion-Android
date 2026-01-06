package dev.minios.pdaiv1.core.imageprocessing.blurhash

import android.graphics.Bitmap
import com.vanniktech.blurhash.BlurHash
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single

/**
 * Encodes Bitmap images to BlurHash strings for progressive loading placeholders.
 */
class BlurHashEncoder(
    private val processingScheduler: Scheduler,
) {

    /**
     * Encodes a bitmap to a BlurHash string synchronously.
     * Use this for inline processing where async is not needed.
     */
    fun encodeSync(
        bitmap: Bitmap,
        componentX: Int = DEFAULT_COMPONENT_X,
        componentY: Int = DEFAULT_COMPONENT_Y,
    ): String = BlurHash.encode(bitmap, componentX, componentY) ?: ""

    /**
     * Encodes a bitmap to a BlurHash string.
     * @param bitmap The source bitmap to encode
     * @param componentX Horizontal components (1-9), higher = more detail
     * @param componentY Vertical components (1-9), higher = more detail
     * @return Single emitting the BlurHash string
     */
    fun encode(
        bitmap: Bitmap,
        componentX: Int = DEFAULT_COMPONENT_X,
        componentY: Int = DEFAULT_COMPONENT_Y,
    ): Single<String> = Single
        .fromCallable {
            BlurHash.encode(bitmap, componentX, componentY)
                ?: throw IllegalStateException("Failed to encode BlurHash")
        }
        .subscribeOn(processingScheduler)

    companion object {
        const val DEFAULT_COMPONENT_X = 4
        const val DEFAULT_COMPONENT_Y = 3
    }
}
