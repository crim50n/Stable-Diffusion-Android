package dev.minios.pdaiv1.domain.usecase.settings

import dev.minios.pdaiv1.domain.feature.auth.AuthorizationCredentials
import dev.minios.pdaiv1.domain.mocks.mockConfiguration
import dev.minios.pdaiv1.domain.usecase.connectivity.TestSwarmUiConnectivityUseCase
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ConnectToSwarmUiUseCaseImplTest {

    private val testScheduler = TestScheduler()
    private val stubThrowable = Throwable("Something went wrong.")
    private val stubGetConfigurationUseCase = mockk<GetConfigurationUseCase>()
    private val stubSetServerConfigurationUseCase = mockk<SetServerConfigurationUseCase>()
    private val stubTestSwarmUiConnectivityUseCase = mockk<TestSwarmUiConnectivityUseCase>()

    private val useCase = ConnectToSwarmUiUseCaseImpl(
        getConfigurationUseCase = stubGetConfigurationUseCase,
        setServerConfigurationUseCase = stubSetServerConfigurationUseCase,
        testSwarmUiConnectivityUseCase = stubTestSwarmUiConnectivityUseCase,
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
    fun `given connection process successful, expected success result value`() {
        val url = "http://localhost:7801"
        val credentials = AuthorizationCredentials.None

        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestSwarmUiConnectivityUseCase(url)
        } returns Completable.complete()

        val testObserver = useCase(url, credentials).test()

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .assertValue(Result.success(Unit))
            .assertComplete()
    }

    @Test
    fun `given connection process failed on get configuration, expected error result value`() {
        val url = "http://localhost:7801"
        val credentials = AuthorizationCredentials.None

        every {
            stubGetConfigurationUseCase()
        } returns Single.error(stubThrowable)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestSwarmUiConnectivityUseCase(url)
        } returns Completable.complete()

        val testObserver = useCase(url, credentials).test()

        testScheduler.advanceTimeBy(30, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .assertValue { it.isFailure && it.exceptionOrNull() == stubThrowable }
            .assertComplete()
    }

    @Test
    fun `given connectivity test failed, expected error result value and configuration rollback`() {
        val url = "http://localhost:7801"
        val credentials = AuthorizationCredentials.None

        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestSwarmUiConnectivityUseCase(url)
        } returns Completable.error(stubThrowable)

        val testObserver = useCase(url, credentials).test()

        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .assertValue { it.isFailure }
            .assertComplete()
    }

    @Test
    fun `given connection process times out, expected timeout error result value`() {
        val url = "http://localhost:7801"
        val credentials = AuthorizationCredentials.None

        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestSwarmUiConnectivityUseCase(url)
        } returns Completable.never()

        val testObserver = useCase(url, credentials).test()

        testScheduler.advanceTimeBy(31, TimeUnit.SECONDS)

        testObserver
            .assertNoErrors()
            .assertValue { it.isFailure && it.exceptionOrNull() is TimeoutException }
            .assertComplete()
    }
}
