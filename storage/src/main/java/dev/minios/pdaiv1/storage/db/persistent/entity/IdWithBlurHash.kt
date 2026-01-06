package dev.minios.pdaiv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import dev.minios.pdaiv1.storage.db.persistent.contract.GenerationResultContract

/**
 * Lightweight projection for gallery listing.
 * Contains only ID and BlurHash for efficient placeholder rendering.
 */
data class IdWithBlurHash(
    @ColumnInfo(name = GenerationResultContract.ID)
    val id: Long,
    @ColumnInfo(name = GenerationResultContract.BLUR_HASH)
    val blurHash: String,
)
