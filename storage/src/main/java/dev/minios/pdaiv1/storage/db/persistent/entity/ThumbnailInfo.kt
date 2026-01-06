package dev.minios.pdaiv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import dev.minios.pdaiv1.storage.db.persistent.contract.GenerationResultContract

/**
 * Lightweight projection for thumbnail loading.
 * Contains only necessary fields to load thumbnail from file.
 */
data class ThumbnailInfo(
    @ColumnInfo(name = GenerationResultContract.ID)
    val id: Long,
    @ColumnInfo(name = GenerationResultContract.MEDIA_PATH)
    val mediaPath: String,
    @ColumnInfo(name = GenerationResultContract.HIDDEN)
    val hidden: Boolean,
    @ColumnInfo(name = GenerationResultContract.BLUR_HASH)
    val blurHash: String,
)
