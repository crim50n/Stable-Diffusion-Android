package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.feature.auth.AuthorizationStore
import dev.minios.pdaiv1.domain.mocks.mockConfiguration
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class SetServerConfigurationUseCaseImplTest {

    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubAuthorizationStore = mockk<AuthorizationStore>()

    private val useCase = SetServerConfigurationUseCaseImpl(
        preferenceManager = stubPreferenceManager,
        authorizationStore = stubAuthorizationStore,
    )

    @Test
    fun `given configuration apply success, expected complete value`() {
        every {
            stubAuthorizationStore.storeAuthorizationCredentials(any())
        } returns Unit

        every {
            stubPreferenceManager::source.set(any())
        } returns Unit

        every {
            stubPreferenceManager::automatic1111ServerUrl.set(any())
        } returns Unit

        every {
            stubPreferenceManager::swarmUiModel.set(any())
        } returns Unit

        every {
            stubPreferenceManager::swarmUiServerUrl.set(any())
        } returns Unit

        every {
            stubPreferenceManager::demoMode.set(any())
        } returns Unit

        every {
            stubPreferenceManager::hordeApiKey.set(any())
        } returns Unit

        every {
            stubPreferenceManager::openAiApiKey.set(any())
        } returns Unit

        every {
            stubPreferenceManager::huggingFaceApiKey.set(any())
        } returns Unit

        every {
            stubPreferenceManager::huggingFaceModel.set(any())
        } returns Unit

        every {
            stubPreferenceManager::stabilityAiApiKey.set(any())
        } returns Unit

        every {
            stubPreferenceManager::stabilityAiEngineId.set(any())
        } returns Unit

        every {
            stubPreferenceManager::localOnnxModelId.set(any())
        } returns Unit

        every {
            stubPreferenceManager::localOnnxCustomModelPath.set(any())
        } returns Unit

        every {
            stubPreferenceManager::localMediaPipeModelId.set(any())
        } returns Unit

        every {
            stubPreferenceManager::localMediaPipeCustomModelPath.set(any())
        } returns Unit

        every {
            stubPreferenceManager::falAiApiKey.set(any())
        } returns Unit

        every {
            stubPreferenceManager::localQnnModelId.set(any())
        } returns Unit

        every {
            stubPreferenceManager::localQnnCustomModelPath.set(any())
        } returns Unit

        useCase
            .invoke(mockConfiguration)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }
}
