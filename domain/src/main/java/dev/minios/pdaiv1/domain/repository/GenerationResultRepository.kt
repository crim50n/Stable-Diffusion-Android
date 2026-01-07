package dev.minios.pdaiv1.domain.repository

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.MediaStoreInfo
import dev.minios.pdaiv1.domain.entity.ThumbnailData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface GenerationResultRepository {

    fun getAll(): Single<List<AiGenerationResult>>

    fun getAllIds(): Single<List<Long>>

    fun getAllIdsWithBlurHash(): Single<List<Pair<Long, String>>>

    fun getThumbnailInfoByIds(idList: List<Long>): Single<List<ThumbnailData>>

    fun getPage(limit: Int, offset: Int): Single<List<AiGenerationResult>>

    fun getMediaStoreInfo(): Single<MediaStoreInfo>

    fun getById(id: Long): Single<AiGenerationResult>

    fun getByIds(idList: List<Long>): Single<List<AiGenerationResult>>

    /**
     * Returns raw data without loading full images from files.
     * Use this for thumbnail loading where only mediaPath is needed.
     */
    fun getByIdsRaw(idList: List<Long>): Single<List<AiGenerationResult>>

    fun insert(result: AiGenerationResult): Single<Long>

    fun deleteById(id: Long): Completable

    fun deleteByIdList(idList: List<Long>): Completable

    fun deleteAll(): Completable

    fun deleteAllUnliked(): Completable

    fun toggleVisibility(id: Long): Single<Boolean>

    fun toggleLike(id: Long): Single<Boolean>

    fun likeByIds(idList: List<Long>): Completable

    fun unlikeByIds(idList: List<Long>): Completable

    fun hideByIds(idList: List<Long>): Completable

    fun unhideByIds(idList: List<Long>): Completable

    /**
     * Migrates existing gallery items from base64 storage to file-based storage.
     * This runs in the background at app startup.
     */
    fun migrateBase64ToFiles(): Completable
}
