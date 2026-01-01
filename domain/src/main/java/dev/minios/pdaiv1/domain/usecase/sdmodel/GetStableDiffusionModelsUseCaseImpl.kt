package dev.minios.pdaiv1.domain.usecase.sdmodel

import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.entity.StableDiffusionModel
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.ServerConfigurationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionModelsRepository
import io.reactivex.rxjava3.core.Single

internal class GetStableDiffusionModelsUseCaseImpl(
    private val preferenceManager: PreferenceManager,
    private val serverConfigurationRepository: ServerConfigurationRepository,
    private val sdModelsRepository: StableDiffusionModelsRepository,
) : GetStableDiffusionModelsUseCase {

    override operator fun invoke(): Single<List<Pair<StableDiffusionModel, Boolean>>> {
        // Only fetch models if A1111/Forge is the active source
        if (preferenceManager.source != ServerSource.AUTOMATIC1111) {
            return Single.just(emptyList())
        }
        return serverConfigurationRepository
            .fetchAndGetConfiguration()
            .flatMap { config ->
                sdModelsRepository
                    .fetchAndGetModels()
                    .map { sdModels -> config to sdModels }
            }
            .map { (config, sdModels) ->
                sdModels.map { model ->
                    model to (config.sdModelCheckpoint == model.title)
                }
            }
    }
}
