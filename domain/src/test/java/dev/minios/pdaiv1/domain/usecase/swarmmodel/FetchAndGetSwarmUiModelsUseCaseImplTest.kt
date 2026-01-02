package dev.minios.pdaiv1.domain.usecase.swarmmodel

import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.mocks.mockSwarmUiModels
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.SwarmUiModelsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class FetchAndGetSwarmUiModelsUseCaseImplTest {

    private val stubPreferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val stubRepository = mockk<SwarmUiModelsRepository>()

    private val useCase = FetchAndGetSwarmUiModelsUseCaseImpl(
        preferenceManager = stubPreferenceManager,
        repository = stubRepository,
    )

    @Test
    fun `given source is not SWARM_UI, expected empty list`() {
        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is SWARM_UI and repository returns models, expected models list`() {
        every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
        every { stubPreferenceManager.swarmUiModel } returns "mock-model"
        every { stubRepository.fetchAndGetModels() } returns Single.just(mockSwarmUiModels)

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(mockSwarmUiModels)
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is SWARM_UI and current model not in list, expected model updated to first`() {
        every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
        every { stubPreferenceManager.swarmUiModel } returns "nonexistent-model"
        every { stubRepository.fetchAndGetModels() } returns Single.just(mockSwarmUiModels)

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(mockSwarmUiModels)
            .await()
            .assertComplete()

        verify { stubPreferenceManager.swarmUiModel = "mock-model" }
    }

    @Test
    fun `given source is SWARM_UI and repository returns empty list, expected empty list`() {
        every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
        every { stubPreferenceManager.swarmUiModel } returns "mock-model"
        every { stubRepository.fetchAndGetModels() } returns Single.just(emptyList())

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(emptyList())
            .await()
            .assertComplete()

        verify { stubPreferenceManager.swarmUiModel = "" }
    }

    @Test
    fun `given source is SWARM_UI and repository throws error, expected error`() {
        val stubException = Throwable("Network error")

        every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
        every { stubRepository.fetchAndGetModels() } returns Single.error(stubException)

        useCase()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
