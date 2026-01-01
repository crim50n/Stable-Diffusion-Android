package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapDomainToEntity
import dev.minios.pdaiv1.data.mappers.mapEntityToDomain
import dev.minios.pdaiv1.domain.datasource.EmbeddingsDataSource
import dev.minios.pdaiv1.domain.entity.Embedding
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionEmbeddingDao
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionEmbeddingEntity

internal class EmbeddingsLocalDataSource(
    private val dao: StableDiffusionEmbeddingDao,
) : EmbeddingsDataSource.Local {

    override fun getEmbeddings() = dao
        .queryAll()
        .map(List<StableDiffusionEmbeddingEntity>::mapEntityToDomain)

    override fun insertEmbeddings(list: List<Embedding>) = dao
        .deleteAll()
        .andThen(dao.insertList(list.mapDomainToEntity()))
}
