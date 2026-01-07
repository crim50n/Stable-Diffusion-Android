package dev.minios.pdaiv1.domain.datasource

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ThumbnailData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

sealed interface GenerationResultDataSource {

    interface Local : GenerationResultDataSource {
        fun insert(result: AiGenerationResult): Single<Long>
        fun queryAll(): Single<List<AiGenerationResult>>
        fun queryAllIds(): Single<List<Long>>
        fun queryAllIdsWithBlurHash(): Single<List<Pair<Long, String>>>
        fun queryThumbnailInfoByIdList(idList: List<Long>): Single<List<ThumbnailData>>
        fun queryPage(limit: Int, offset: Int): Single<List<AiGenerationResult>>
        fun queryById(id: Long): Single<AiGenerationResult>
        fun queryByIdList(idList: List<Long>): Single<List<AiGenerationResult>>
        fun deleteById(id: Long): Completable
        fun deleteByIdList(idList: List<Long>): Completable
        fun deleteAll(): Completable
        fun deleteAllUnliked(): Completable
        fun likeByIds(idList: List<Long>): Completable
        fun unlikeByIds(idList: List<Long>): Completable
        fun hideByIds(idList: List<Long>): Completable
        fun unhideByIds(idList: List<Long>): Completable
    }
}
