package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapDomainToEntity
import dev.minios.pdaiv1.data.mappers.mapEntityToDomain
import dev.minios.pdaiv1.domain.datasource.LorasDataSource
import dev.minios.pdaiv1.domain.entity.LoRA
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionLoraDao
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionLoraEntity

internal class LorasLocalDataSource(
    private val dao: StableDiffusionLoraDao,
) : LorasDataSource.Local {

    override fun getLoras() = dao
        .queryAll()
        .map(List<StableDiffusionLoraEntity>::mapEntityToDomain)

    override fun insertLoras(loras: List<LoRA>) = dao
        .deleteAll()
        .andThen(dao.insertList(loras.mapDomainToEntity()))
}
