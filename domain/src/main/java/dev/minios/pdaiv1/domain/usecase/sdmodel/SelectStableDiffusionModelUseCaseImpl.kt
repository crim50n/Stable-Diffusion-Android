package dev.minios.pdaiv1.domain.usecase.sdmodel

import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.ServerConfigurationRepository
import io.reactivex.rxjava3.core.Completable

internal class SelectStableDiffusionModelUseCaseImpl(
    private val serverConfigurationRepository: ServerConfigurationRepository,
    private val preferenceManager: PreferenceManager,
) : SelectStableDiffusionModelUseCase {

    override operator fun invoke(modelName: String): Completable = serverConfigurationRepository
        .getConfiguration()
        .map { config ->
            preferenceManager.sdModel = modelName
            config.copy(sdModelCheckpoint = modelName)
        }
        .flatMapCompletable(serverConfigurationRepository::updateConfiguration)
        .andThen(serverConfigurationRepository.fetchConfiguration())
}
