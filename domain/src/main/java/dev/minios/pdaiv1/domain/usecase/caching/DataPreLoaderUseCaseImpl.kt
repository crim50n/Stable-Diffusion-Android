package dev.minios.pdaiv1.domain.usecase.caching

import dev.minios.pdaiv1.core.common.log.debugLog
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.domain.entity.FeatureTag
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.EmbeddingsRepository
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import dev.minios.pdaiv1.domain.repository.LorasRepository
import dev.minios.pdaiv1.domain.repository.ServerConfigurationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionHyperNetworksRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionModelsRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionSamplersRepository
import io.reactivex.rxjava3.core.Completable

internal class DataPreLoaderUseCaseImpl(
    private val preferenceManager: PreferenceManager,
    private val serverConfigurationRepository: ServerConfigurationRepository,
    private val sdModelsRepository: StableDiffusionModelsRepository,
    private val sdSamplersRepository: StableDiffusionSamplersRepository,
    private val sdLorasRepository: LorasRepository,
    private val sdHyperNetworksRepository: StableDiffusionHyperNetworksRepository,
    private val sdEmbeddingsRepository: EmbeddingsRepository,
    private val generationResultRepository: GenerationResultRepository,
) : DataPreLoaderUseCase {

    override operator fun invoke(): Completable {
        // Always run migration first (will be no-op if already migrated)
        val migrationCompletable = generationResultRepository
            .migrateBase64ToFiles()
            .doOnSubscribe { debugLog("Starting base64 to files migration...") }
            .doOnComplete { debugLog("Base64 to files migration completed.") }
            .onErrorComplete { t ->
                errorLog(t, "Base64 to files migration failed")
                true // Continue even if migration fails
            }

        val source = preferenceManager.source
        val requiresServerData = source.featureTags.contains(FeatureTag.OwnServer)

        // Skip server data fetching for sources that don't need it (Horde, HuggingFace, OpenAI, StabilityAI, FalAI, local)
        if (!requiresServerData) {
            return migrationCompletable
        }

        // Only fetch server configuration and related data for A1111/SwarmUI
        return migrationCompletable
            .andThen(serverConfigurationRepository.fetchConfiguration())
            .andThen(sdModelsRepository.fetchModels())
            .andThen(sdSamplersRepository.fetchSamplers())
            .andThen(sdLorasRepository.fetchLoras())
            .andThen(sdHyperNetworksRepository.fetchHyperNetworks())
            .andThen(sdEmbeddingsRepository.fetchEmbeddings())
    }
}
