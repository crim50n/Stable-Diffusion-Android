package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.mocks.mockConfiguration
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.connectivity.TestFalAiApiKeyUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ConnectToFalAiUseCaseImplTest {

    private val stubThrowable = Throwable("Something went wrong.")
    private val stubGetConfigurationUseCase = mockk<GetConfigurationUseCase>()
    private val stubSetServerConfigurationUseCase = mockk<SetServerConfigurationUseCase>()
    private val stubTestFalAiApiKeyUseCase = mockk<TestFalAiApiKeyUseCase>()
    private val stubPreferenceManager = mockk<PreferenceManager>(relaxed = true)

    private val testScheduler = TestScheduler()

    private val useCase = ConnectToFalAiUseCaseImpl(
        getConfigurationUseCase = stubGetConfigurationUseCase,
        setServerConfigurationUseCase = stubSetServerConfigurationUseCase,
        testFalAiApiKeyUseCase = stubTestFalAiApiKeyUseCase,
        preferenceManager = stubPreferenceManager,
    )

    @Before
    fun setUp() {
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
    }

    @Test
    fun `given connection process successful, API key is valid, expected success result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestFalAiApiKeyUseCase()
        } returns Single.just(true)

        val testObserver = useCase("test-api-key", "fal-ai/flux/schnell")
            .test()

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .await()
            .assertValue(Result.success(Unit))
            .assertComplete()

        verify { stubPreferenceManager.falAiSelectedEndpointId = "fal-ai/flux/schnell" }
    }

    @Test
    fun `given connection process successful, API key is NOT valid, expected failure result value with rollback`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestFalAiApiKeyUseCase()
        } returns Single.just(false)

        val testObserver = useCase("bad-api-key", "fal-ai/flux/schnell")
            .test()

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .await()
            .assertValue { actual ->
                actual.isFailure
                        && actual.exceptionOrNull() is IllegalStateException
                        && actual.exceptionOrNull()?.message == "Bad key"
            }
            .assertComplete()
    }

    @Test
    fun `given get configuration fails, expected failure result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.error(stubThrowable)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestFalAiApiKeyUseCase()
        } returns Single.just(true)

        val testObserver = useCase("test-api-key", "fal-ai/flux/schnell")
            .test()

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .await()
            .assertValue(Result.failure(stubThrowable))
            .assertComplete()
    }

    @Test
    fun `given set configuration fails, expected failure result value with rollback`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.error(stubThrowable) andThen Completable.complete()

        every {
            stubTestFalAiApiKeyUseCase()
        } returns Single.just(true)

        val testObserver = useCase("test-api-key", "fal-ai/flux/schnell")
            .test()

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .await()
            .assertValue(Result.failure(stubThrowable))
            .assertComplete()
    }

    @Test
    fun `given API key test fails with exception, expected failure result value with rollback`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestFalAiApiKeyUseCase()
        } returns Single.error(stubThrowable)

        val testObserver = useCase("test-api-key", "fal-ai/flux/schnell")
            .test()

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .await()
            .assertValue(Result.failure(stubThrowable))
            .assertComplete()
    }
}
