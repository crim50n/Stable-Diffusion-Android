package dev.minios.pdaiv1.data.local

import dev.minios.pdaiv1.data.mappers.mapDomainToEntity
import dev.minios.pdaiv1.data.mappers.mapEntityToDomain
import dev.minios.pdaiv1.domain.datasource.SwarmUiModelsDataSource
import dev.minios.pdaiv1.domain.entity.SwarmUiModel
import dev.minios.pdaiv1.storage.db.cache.dao.SwarmUiModelDao
import dev.minios.pdaiv1.storage.db.cache.entity.SwarmUiModelEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class SwarmUiModelsLocalDataSource(
    private val dao: SwarmUiModelDao,
) : SwarmUiModelsDataSource.Local {

    override fun getModels(): Single<List<SwarmUiModel>> = dao
        .queryAll()
        .map(List<SwarmUiModelEntity>::mapEntityToDomain)

    override fun insertModels(models: List<SwarmUiModel>): Completable = dao
        .deleteAll()
        .andThen(dao.insertList(models.mapDomainToEntity()))
}
