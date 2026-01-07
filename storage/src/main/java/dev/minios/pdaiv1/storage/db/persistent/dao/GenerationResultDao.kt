package dev.minios.pdaiv1.storage.db.persistent.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.minios.pdaiv1.storage.db.persistent.contract.GenerationResultContract
import dev.minios.pdaiv1.storage.db.persistent.entity.GenerationResultEntity
import dev.minios.pdaiv1.storage.db.persistent.entity.IdWithBlurHash
import dev.minios.pdaiv1.storage.db.persistent.entity.ThumbnailInfo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface GenerationResultDao {

    @Query("SELECT * FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC")
    fun query(): Single<List<GenerationResultEntity>>

    @Query("SELECT ${GenerationResultContract.ID} FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC")
    fun queryAllIds(): Single<List<Long>>

    @Query("SELECT ${GenerationResultContract.ID}, ${GenerationResultContract.BLUR_HASH} FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC")
    fun queryAllIdsWithBlurHash(): Single<List<IdWithBlurHash>>

    @Query("SELECT ${GenerationResultContract.ID}, ${GenerationResultContract.MEDIA_PATH}, ${GenerationResultContract.HIDDEN}, ${GenerationResultContract.BLUR_HASH} FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} IN (:idList)")
    fun queryThumbnailInfoByIdList(idList: List<Long>): Single<List<ThumbnailInfo>>

    @Query("SELECT ${GenerationResultContract.ID} FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC LIMIT :limit OFFSET :offset")
    fun queryPageIds(limit: Int, offset: Int): Single<List<Long>>

    @Query("SELECT * FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC LIMIT :limit OFFSET :offset ")
    fun queryPage(limit: Int, offset: Int): Single<List<GenerationResultEntity>>

    @Query("SELECT * FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} = :id LIMIT 1")
    fun queryById(id: Long): Single<GenerationResultEntity>

    @Query("SELECT * FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} IN (:idList)")
    fun queryByIdList(idList: List<Long>): Single<List<GenerationResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: GenerationResultEntity): Single<Long>

    @Query("DELETE FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} = :id")
    fun deleteById(id: Long): Completable

    @Query("DELETE FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} IN (:idList)")
    fun deleteByIdList(idList: List<Long>): Completable

    @Query("DELETE FROM ${GenerationResultContract.TABLE}")
    fun deleteAll(): Completable

    @Query("DELETE FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.LIKED} = 0")
    fun deleteAllUnliked(): Completable

    @Query("UPDATE ${GenerationResultContract.TABLE} SET ${GenerationResultContract.LIKED} = 1 WHERE ${GenerationResultContract.ID} IN (:idList)")
    fun likeByIds(idList: List<Long>): Completable

    @Query("UPDATE ${GenerationResultContract.TABLE} SET ${GenerationResultContract.LIKED} = 0 WHERE ${GenerationResultContract.ID} IN (:idList)")
    fun unlikeByIds(idList: List<Long>): Completable

    @Query("UPDATE ${GenerationResultContract.TABLE} SET ${GenerationResultContract.HIDDEN} = 1 WHERE ${GenerationResultContract.ID} IN (:idList)")
    fun hideByIds(idList: List<Long>): Completable

    @Query("UPDATE ${GenerationResultContract.TABLE} SET ${GenerationResultContract.HIDDEN} = 0 WHERE ${GenerationResultContract.ID} IN (:idList)")
    fun unhideByIds(idList: List<Long>): Completable
}
