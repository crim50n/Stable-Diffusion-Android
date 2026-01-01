package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapDomainToEntity
import dev.minios.pdaiv1.data.mappers.mapEntityToDomain
import dev.minios.pdaiv1.domain.datasource.StableDiffusionSamplersDataSource
import dev.minios.pdaiv1.domain.entity.StableDiffusionSampler
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionSamplerDao
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionSamplerEntity

internal class StableDiffusionSamplersLocalDataSource(
    private val dao: StableDiffusionSamplerDao,
) : StableDiffusionSamplersDataSource.Local {

    override fun getSamplers() = dao
        .queryAll()
        .map(List<StableDiffusionSamplerEntity>::mapEntityToDomain)

    override fun insertSamplers(samplers: List<StableDiffusionSampler>) = dao
        .deleteAll()
        .andThen(dao.insertList(samplers.mapDomainToEntity()))
}
