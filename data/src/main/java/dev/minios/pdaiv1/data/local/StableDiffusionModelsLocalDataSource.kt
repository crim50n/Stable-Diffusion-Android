package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapDomainToEntity
import dev.minios.pdaiv1.data.mappers.mapEntityToDomain
import dev.minios.pdaiv1.domain.datasource.StableDiffusionModelsDataSource
import dev.minios.pdaiv1.domain.entity.StableDiffusionModel
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionModelDao
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionModelEntity

internal class StableDiffusionModelsLocalDataSource(
    private val dao: StableDiffusionModelDao,
) : StableDiffusionModelsDataSource.Local {

    override fun getModels() = dao
        .queryAll()
        .map(List<StableDiffusionModelEntity>::mapEntityToDomain)

    override fun insertModels(models: List<StableDiffusionModel>) = dao
        .deleteAll()
        .andThen(dao.insertList(models.mapDomainToEntity()))
}
