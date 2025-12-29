package com.shifthackz.aisdv1.storage.db.persistent.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifthackz.aisdv1.storage.db.persistent.contract.FalAiEndpointContract
import com.shifthackz.aisdv1.storage.db.persistent.entity.FalAiEndpointEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface FalAiEndpointDao {

    @Query("SELECT * FROM ${FalAiEndpointContract.TABLE}")
    fun observeAll(): Flowable<List<FalAiEndpointEntity>>

    @Query("SELECT * FROM ${FalAiEndpointContract.TABLE}")
    fun queryAll(): Single<List<FalAiEndpointEntity>>

    @Query("SELECT * FROM ${FalAiEndpointContract.TABLE} WHERE ${FalAiEndpointContract.ID} = :id")
    fun queryById(id: String): Single<FalAiEndpointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: FalAiEndpointEntity): Completable

    @Query("DELETE FROM ${FalAiEndpointContract.TABLE} WHERE ${FalAiEndpointContract.ID} = :id")
    fun deleteById(id: String): Completable

    @Query("DELETE FROM ${FalAiEndpointContract.TABLE}")
    fun deleteAll(): Completable
}
