package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapToDomain
import dev.minios.pdaiv1.data.mappers.mapToEntity
import dev.minios.pdaiv1.domain.datasource.ServerConfigurationDataSource
import dev.minios.pdaiv1.domain.entity.ServerConfiguration
import dev.minios.pdaiv1.storage.db.cache.dao.ServerConfigurationDao
import dev.minios.pdaiv1.storage.db.cache.entity.ServerConfigurationEntity

internal class ServerConfigurationLocalDataSource(
    private val dao: ServerConfigurationDao,
) : ServerConfigurationDataSource.Local {

    override fun save(configuration: ServerConfiguration) = dao
        .insert(configuration.mapToEntity())

    override fun get() = dao
        .query()
        .map(ServerConfigurationEntity::mapToDomain)
}
