package dev.minios.pdaiv1.domain.usecase.caching

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.EmbeddingsRepository
import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import dev.minios.pdaiv1.domain.repository.LorasRepository
import dev.minios.pdaiv1.domain.repository.ServerConfigurationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionHyperNetworksRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionModelsRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionSamplersRepository
import io.reactivex.rxjava3.core.Completable
import org.junit.Test

class DataPreLoaderUseCaseImplTest {

    private val stubPreferenceManager = mock<PreferenceManager>()
    private val stubServerConfigurationRepository = mock<ServerConfigurationRepository>()
    private val stubStableDiffusionModelsRepository = mock<StableDiffusionModelsRepository>()
    private val stubStableDiffusionSamplersRepository = mock<StableDiffusionSamplersRepository>()
    private val stubLorasRepository = mock<LorasRepository>()
    private val stubStableDiffusionHyperNetworksRepository = mock<StableDiffusionHyperNetworksRepository>()
    private val stubEmbeddingsRepository = mock<EmbeddingsRepository>()
    private val stubGenerationResultRepository = mock<GenerationResultRepository>()

    private val useCase = DataPreLoaderUseCaseImpl(
        preferenceManager = stubPreferenceManager,
        serverConfigurationRepository = stubServerConfigurationRepository,
        sdModelsRepository = stubStableDiffusionModelsRepository,
        sdSamplersRepository = stubStableDiffusionSamplersRepository,
        sdLorasRepository = stubLorasRepository,
        sdHyperNetworksRepository = stubStableDiffusionHyperNetworksRepository,
        sdEmbeddingsRepository = stubEmbeddingsRepository,
        generationResultRepository = stubGenerationResultRepository,
    )

    @Test
    fun `given all data fetched successfully, source is AUTOMATIC1111, expected complete value`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubGenerationResultRepository.migrateBase64ToFiles())
            .thenReturn(Completable.complete())

        whenever(stubServerConfigurationRepository.fetchConfiguration())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionModelsRepository.fetchModels())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionSamplersRepository.fetchSamplers())
            .thenReturn(Completable.complete())

        whenever(stubLorasRepository.fetchLoras())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks())
            .thenReturn(Completable.complete())

        whenever(stubEmbeddingsRepository.fetchEmbeddings())
            .thenReturn(Completable.complete())

        useCase()
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is FAL_AI, expected only migration runs and completes`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.FAL_AI)

        whenever(stubGenerationResultRepository.migrateBase64ToFiles())
            .thenReturn(Completable.complete())

        useCase()
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given configuration fetch failed, expected error value`() {
        val stubException = Throwable("Can not fetch configuration.")

        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubGenerationResultRepository.migrateBase64ToFiles())
            .thenReturn(Completable.complete())

        whenever(stubServerConfigurationRepository.fetchConfiguration())
            .thenReturn(Completable.error(stubException))

        whenever(stubStableDiffusionModelsRepository.fetchModels())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionSamplersRepository.fetchSamplers())
            .thenReturn(Completable.complete())

        whenever(stubLorasRepository.fetchLoras())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks())
            .thenReturn(Completable.complete())

        whenever(stubEmbeddingsRepository.fetchEmbeddings())
            .thenReturn(Completable.complete())

        useCase()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given models fetch failed, expected error value`() {
        val stubException = Throwable("Can not fetch models.")

        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubGenerationResultRepository.migrateBase64ToFiles())
            .thenReturn(Completable.complete())

        whenever(stubServerConfigurationRepository.fetchConfiguration())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionModelsRepository.fetchModels())
            .thenReturn(Completable.error(stubException))

        whenever(stubStableDiffusionSamplersRepository.fetchSamplers())
            .thenReturn(Completable.complete())

        whenever(stubLorasRepository.fetchLoras())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks())
            .thenReturn(Completable.complete())

        whenever(stubEmbeddingsRepository.fetchEmbeddings())
            .thenReturn(Completable.complete())

        useCase()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given samplers fetch failed, expected error value`() {
        val stubException = Throwable("Can not fetch samplers.")

        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubGenerationResultRepository.migrateBase64ToFiles())
            .thenReturn(Completable.complete())

        whenever(stubServerConfigurationRepository.fetchConfiguration())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionModelsRepository.fetchModels())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionSamplersRepository.fetchSamplers())
            .thenReturn(Completable.error(stubException))

        whenever(stubLorasRepository.fetchLoras())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks())
            .thenReturn(Completable.complete())

        whenever(stubEmbeddingsRepository.fetchEmbeddings())
            .thenReturn(Completable.complete())

        useCase()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given loras fetch failed, expected error value`() {
        val stubException = Throwable("Can not fetch loras.")

        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubGenerationResultRepository.migrateBase64ToFiles())
            .thenReturn(Completable.complete())

        whenever(stubServerConfigurationRepository.fetchConfiguration())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionModelsRepository.fetchModels())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionSamplersRepository.fetchSamplers())
            .thenReturn(Completable.complete())

        whenever(stubLorasRepository.fetchLoras())
            .thenReturn(Completable.error(stubException))

        whenever(stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks())
            .thenReturn(Completable.complete())

        whenever(stubEmbeddingsRepository.fetchEmbeddings())
            .thenReturn(Completable.complete())

        useCase()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given hypernetworks fetch failed, expected error value`() {
        val stubException = Throwable("Can not fetch hypernetworks.")

        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubGenerationResultRepository.migrateBase64ToFiles())
            .thenReturn(Completable.complete())

        whenever(stubServerConfigurationRepository.fetchConfiguration())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionModelsRepository.fetchModels())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionSamplersRepository.fetchSamplers())
            .thenReturn(Completable.complete())

        whenever(stubLorasRepository.fetchLoras())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks())
            .thenReturn(Completable.error(stubException))

        whenever(stubEmbeddingsRepository.fetchEmbeddings())
            .thenReturn(Completable.complete())

        useCase()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given embeddings fetch failed, expected error value`() {
        val stubException = Throwable("Can not fetch embeddings.")

        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubGenerationResultRepository.migrateBase64ToFiles())
            .thenReturn(Completable.complete())

        whenever(stubServerConfigurationRepository.fetchConfiguration())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionModelsRepository.fetchModels())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionSamplersRepository.fetchSamplers())
            .thenReturn(Completable.complete())

        whenever(stubLorasRepository.fetchLoras())
            .thenReturn(Completable.complete())

        whenever(stubStableDiffusionHyperNetworksRepository.fetchHyperNetworks())
            .thenReturn(Completable.complete())

        whenever(stubEmbeddingsRepository.fetchEmbeddings())
            .thenReturn(Completable.error(stubException))

        useCase()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
