package dev.minios.pdaiv1.domain.entity

/**
 * Lightweight data class for thumbnail loading.
 * Contains only necessary fields without heavy Base64 image data.
 */
data class ThumbnailData(
    val id: Long,
    val mediaPath: String,
    val hidden: Boolean,
    val blurHash: String,
)
