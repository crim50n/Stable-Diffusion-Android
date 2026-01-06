package dev.minios.pdaiv1.core.imageprocessing.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

fun base64ToBitmap(base64: String): Bitmap {
    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

/**
 * Decodes base64 to bitmap with subsampling for memory efficiency.
 * Uses inSampleSize to decode smaller image directly, avoiding
 * full-size decode + scale overhead.
 *
 * @param base64 The base64 encoded image string
 * @param targetSize The target size (width/height) for the thumbnail
 * @return Subsampled bitmap fitting within targetSize
 */
fun base64ToThumbnailBitmap(base64: String, targetSize: Int): Bitmap {
    val imageBytes = Base64.decode(base64, Base64.DEFAULT)

    // First pass: decode bounds only (no memory allocation for pixels)
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

    // Calculate inSampleSize for efficient decoding
    options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, targetSize)
    options.inJustDecodeBounds = false

    // Second pass: decode with subsampling
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        ?: throw IllegalArgumentException("Failed to decode bitmap")
}

/**
 * Calculates optimal inSampleSize for decoding.
 * inSampleSize must be power of 2 for best performance.
 */
private fun calculateInSampleSize(width: Int, height: Int, targetSize: Int): Int {
    var inSampleSize = 1
    if (width > targetSize || height > targetSize) {
        val halfWidth = width / 2
        val halfHeight = height / 2
        // Calculate largest inSampleSize that keeps both dimensions >= targetSize
        while ((halfWidth / inSampleSize) >= targetSize &&
            (halfHeight / inSampleSize) >= targetSize) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}

fun base64DefaultToNoWrap(base64Default: String): String {
    val byteArray = Base64.decode(base64Default, Base64.DEFAULT)
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}
