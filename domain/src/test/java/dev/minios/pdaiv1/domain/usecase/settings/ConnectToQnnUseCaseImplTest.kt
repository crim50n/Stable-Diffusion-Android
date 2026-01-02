package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.mocks.mockConfiguration
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class ConnectToQnnUseCaseImplTest {

    private val stubThrowable = Throwable("Something went wrong.")
    private val stubGetConfigurationUseCase = mockk<GetConfigurationUseCase>()
    private val stubSetServerConfigurationUseCase = mockk<SetServerConfigurationUseCase>()
    private val stubPreferenceManager = mockk<PreferenceManager>(relaxed = true)

    private val useCase = ConnectToQnnUseCaseImpl(
        getConfigurationUseCase = stubGetConfigurationUseCase,
        setServerConfigurationUseCase = stubSetServerConfigurationUseCase,
        preferenceManager = stubPreferenceManager,
    )

    @Test
    fun `given connection process successful with CPU mode, expected success result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        useCase("qnn-model-id", runOnCpu = true)
            .test()
            .assertNoErrors()
            .assertValue(Result.success(Unit))
            .await()
            .assertComplete()

        verify { stubPreferenceManager.localQnnRunOnCpu = true }
    }

    @Test
    fun `given connection process successful with NPU mode, expected success result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        useCase("qnn-model-id", runOnCpu = false)
            .test()
            .assertNoErrors()
            .assertValue(Result.success(Unit))
            .await()
            .assertComplete()

        verify { stubPreferenceManager.localQnnRunOnCpu = false }
    }

    @Test
    fun `given connection process failed on get configuration, expected error result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.error(stubThrowable)

        useCase("qnn-model-id", runOnCpu = true)
            .test()
            .assertNoErrors()
            .assertValue(Result.failure(stubThrowable))
            .await()
            .assertComplete()
    }

    @Test
    fun `given connection process failed on set configuration, expected error result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.error(stubThrowable)

        useCase("qnn-model-id", runOnCpu = true)
            .test()
            .assertNoErrors()
            .assertValue(Result.failure(stubThrowable))
            .await()
            .assertComplete()
    }
}
