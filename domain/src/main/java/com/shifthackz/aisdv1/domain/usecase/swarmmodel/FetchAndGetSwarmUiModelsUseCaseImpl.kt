package com.shifthackz.aisdv1.domain.usecase.swarmmodel

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.SwarmUiModel
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.SwarmUiModelsRepository
import io.reactivex.rxjava3.core.Single

internal class FetchAndGetSwarmUiModelsUseCaseImpl(
    private val preferenceManager: PreferenceManager,
    private val repository: SwarmUiModelsRepository,
) : FetchAndGetSwarmUiModelsUseCase {

    override fun invoke(): Single<List<SwarmUiModel>> {
        if (preferenceManager.source != ServerSource.SWARM_UI) {
            return Single.just(emptyList())
        }
        return repository
            .fetchAndGetModels()
            .map { models ->
                if (!models.map(SwarmUiModel::name).contains(preferenceManager.swarmUiModel)) {
                    preferenceManager.swarmUiModel = models.firstOrNull()?.name ?: ""
                }
                models
            }
    }
}
