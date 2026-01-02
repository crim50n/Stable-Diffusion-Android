package dev.minios.pdaiv1.domain.usecase.connectivity

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.repository.SwarmUiGenerationRepository
import io.reactivex.rxjava3.core.Completable
import org.junit.Test

class TestSwarmUiConnectivityUseCaseImplTest {

    private val stubRepository = mock<SwarmUiGenerationRepository>()

    private val useCase = TestSwarmUiConnectivityUseCaseImpl(stubRepository)

    @Test
    fun `given repository check successful, expected complete`() {
        val url = "http://localhost:7801"

        whenever(stubRepository.checkApiAvailability(url))
            .thenReturn(Completable.complete())

        useCase(url)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given repository check failed, expected error`() {
        val url = "http://localhost:7801"
        val stubException = Throwable("Connection refused")

        whenever(stubRepository.checkApiAvailability(url))
            .thenReturn(Completable.error(stubException))

        useCase(url)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }
}
