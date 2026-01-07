package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapDomainToEntity
import dev.minios.pdaiv1.data.mappers.mapEntityToDomain
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ThumbnailData
import dev.minios.pdaiv1.storage.db.persistent.dao.GenerationResultDao
import dev.minios.pdaiv1.storage.db.persistent.entity.GenerationResultEntity
import io.reactivex.rxjava3.core.Single

internal class GenerationResultLocalDataSource(
    private val dao: GenerationResultDao,
) : GenerationResultDataSource.Local {

    override fun insert(result: AiGenerationResult) = result
        .mapDomainToEntity()
        .let(dao::insert)

    override fun queryAll(): Single<List<AiGenerationResult>> = dao
        .query()
        .map(List<GenerationResultEntity>::mapEntityToDomain)

    override fun queryAllIds(): Single<List<Long>> = dao.queryAllIds()

    override fun queryAllIdsWithBlurHash(): Single<List<Pair<Long, String>>> = dao
        .queryAllIdsWithBlurHash()
        .map { list -> list.map { it.id to it.blurHash } }

    override fun queryThumbnailInfoByIdList(idList: List<Long>): Single<List<ThumbnailData>> = dao
        .queryThumbnailInfoByIdList(idList)
        .map { list -> list.map { ThumbnailData(it.id, it.mediaPath, it.hidden, it.blurHash) } }

    override fun queryPage(limit: Int, offset: Int) = dao
        .queryPage(limit, offset)
        .map(List<GenerationResultEntity>::mapEntityToDomain)

    override fun queryById(id: Long) = dao
        .queryById(id)
        .map(GenerationResultEntity::mapEntityToDomain)

    override fun queryByIdList(idList: List<Long>) = dao
        .queryByIdList(idList)
        .map(List<GenerationResultEntity>::mapEntityToDomain)

    override fun deleteById(id: Long) = dao.deleteById(id)

    override fun deleteByIdList(idList: List<Long>) = dao.deleteByIdList(idList)

    override fun deleteAll() = dao.deleteAll()

    override fun deleteAllUnliked() = dao.deleteAllUnliked()

    override fun likeByIds(idList: List<Long>) = dao.likeByIds(idList)

    override fun unlikeByIds(idList: List<Long>) = dao.unlikeByIds(idList)

    override fun hideByIds(idList: List<Long>) = dao.hideByIds(idList)

    override fun unhideByIds(idList: List<Long>) = dao.unhideByIds(idList)
}
