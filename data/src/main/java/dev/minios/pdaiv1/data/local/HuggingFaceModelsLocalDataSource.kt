package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapDomainToEntity
import dev.minios.pdaiv1.data.mappers.mapEntityToDomain
import dev.minios.pdaiv1.domain.datasource.HuggingFaceModelsDataSource
import dev.minios.pdaiv1.domain.entity.HuggingFaceModel
import dev.minios.pdaiv1.storage.db.persistent.dao.HuggingFaceModelDao
import dev.minios.pdaiv1.storage.db.persistent.entity.HuggingFaceModelEntity

internal class HuggingFaceModelsLocalDataSource(
    private val dao: HuggingFaceModelDao,
) : HuggingFaceModelsDataSource.Local {

    override fun getAll() = dao
        .query()
        .map(List<HuggingFaceModelEntity>::mapEntityToDomain)

    override fun save(models: List<HuggingFaceModel>) = dao
        .deleteAll()
        .andThen(dao.insertList(models.mapDomainToEntity()))
}
