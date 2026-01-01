package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapDomainToEntity
import dev.minios.pdaiv1.data.mappers.mapEntityToDomain
import dev.minios.pdaiv1.domain.datasource.StableDiffusionHyperNetworksDataSource
import dev.minios.pdaiv1.domain.entity.StableDiffusionHyperNetwork
import dev.minios.pdaiv1.storage.db.cache.dao.StableDiffusionHyperNetworkDao
import dev.minios.pdaiv1.storage.db.cache.entity.StableDiffusionHyperNetworkEntity

internal class StableDiffusionHyperNetworksLocalDataSource(
    private val dao: StableDiffusionHyperNetworkDao,
) : StableDiffusionHyperNetworksDataSource.Local {

    override fun getHyperNetworks() = dao
        .queryAll()
        .map(List<StableDiffusionHyperNetworkEntity>::mapEntityToDomain)

    override fun insertHyperNetworks(list: List<StableDiffusionHyperNetwork>) = dao
        .deleteAll()
        .andThen(dao.insertList(list.mapDomainToEntity()))
}
