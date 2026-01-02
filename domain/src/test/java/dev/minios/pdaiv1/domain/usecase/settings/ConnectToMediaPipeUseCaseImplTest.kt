package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.mocks.mockConfiguration
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class ConnectToMediaPipeUseCaseImplTest {

    private val stubThrowable = Throwable("Something went wrong.")
    private val stubGetConfigurationUseCase = mockk<GetConfigurationUseCase>()
    private val stubSetServerConfigurationUseCase = mockk<SetServerConfigurationUseCase>()

    private val useCase = ConnectToMediaPipeUseCaseImpl(
        getConfigurationUseCase = stubGetConfigurationUseCase,
        setServerConfigurationUseCase = stubSetServerConfigurationUseCase,
    )

    @Test
    fun `given connection process successful, expected success result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        useCase("mediapipe-model-id")
            .test()
            .assertNoErrors()
            .assertValue(Result.success(Unit))
            .await()
            .assertComplete()
    }

    @Test
    fun `given connection process failed on get configuration, expected error result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.error(stubThrowable)

        useCase("mediapipe-model-id")
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

        useCase("mediapipe-model-id")
            .test()
            .assertNoErrors()
            .assertValue(Result.failure(stubThrowable))
            .await()
            .assertComplete()
    }
}
