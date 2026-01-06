package dev.minios.pdaiv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.minios.pdaiv1.storage.db.persistent.contract.GenerationResultContract
import java.util.Date

@Entity(tableName = GenerationResultContract.TABLE)
data class GenerationResultEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = GenerationResultContract.ID)
    val id: Long,
    @ColumnInfo(name = GenerationResultContract.IMAGE_BASE_64)
    val imageBase64: String,
    @ColumnInfo(name = GenerationResultContract.ORIGINAL_IMAGE_BASE_64)
    val originalImageBase64: String,
    @ColumnInfo(name = GenerationResultContract.CREATED_AT)
    val createdAt: Date,
    @ColumnInfo(name = GenerationResultContract.GENERATION_TYPE)
    val generationType: String,
    @ColumnInfo(name = GenerationResultContract.PROMPT)
    val prompt: String,
    @ColumnInfo(name = GenerationResultContract.NEGATIVE_PROMPT)
    val negativePrompt: String,
    @ColumnInfo(name = GenerationResultContract.WIDTH)
    val width: Int,
    @ColumnInfo(name = GenerationResultContract.HEIGHT)
    val height: Int,
    @ColumnInfo(name = GenerationResultContract.SAMPLING_STEPS)
    val samplingSteps: Int,
    @ColumnInfo(name = GenerationResultContract.CFG_SCALE)
    val cfgScale: Float,
    @ColumnInfo(name = GenerationResultContract.RESTORE_FACES)
    val restoreFaces: Boolean,
    @ColumnInfo(name = GenerationResultContract.SAMPLER)
    val sampler: String,
    @ColumnInfo(name = GenerationResultContract.SEED)
    val seed: String,
    @ColumnInfo(name = GenerationResultContract.SUB_SEED, defaultValue = "")
    val subSeed: String,
    @ColumnInfo(name = GenerationResultContract.SUB_SEED_STRENGTH, defaultValue = "${0f}")
    val subSeedStrength: Float,
    @ColumnInfo(name = GenerationResultContract.DENOISING_STRENGTH, defaultValue = "${0f}")
    val denoisingStrength: Float,
    @ColumnInfo(name = GenerationResultContract.HIDDEN, defaultValue = "0")
    val hidden: Boolean,
    @ColumnInfo(name = GenerationResultContract.LIKED, defaultValue = "0")
    val liked: Boolean,
    @ColumnInfo(name = GenerationResultContract.MEDIA_PATH, defaultValue = "")
    val mediaPath: String,
    @ColumnInfo(name = GenerationResultContract.INPUT_MEDIA_PATH, defaultValue = "")
    val inputMediaPath: String,
    @ColumnInfo(name = GenerationResultContract.MEDIA_TYPE, defaultValue = "IMAGE")
    val mediaType: String,
    @ColumnInfo(name = GenerationResultContract.MODEL_NAME, defaultValue = "")
    val modelName: String,
    @ColumnInfo(name = GenerationResultContract.BLUR_HASH, defaultValue = "")
    val blurHash: String,
)
