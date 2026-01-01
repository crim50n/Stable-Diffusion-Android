package dev.minios.pdaiv1.domain.usecase.connectivity

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.repository.FalAiGenerationRepository
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class TestFalAiApiKeyUseCaseImplTest {

    private val stubException = Throwable("Failed to validate API key.")
    private val stubFalAiGenerationRepository = mock<FalAiGenerationRepository>()

    private val useCase = TestFalAiApiKeyUseCaseImpl(
        falAiGenerationRepository = stubFalAiGenerationRepository,
    )

    @Test
    fun `given attempt to validate api key, repository returns true, expected true value`() {
        whenever(stubFalAiGenerationRepository.validateApiKey())
            .thenReturn(Single.just(true))

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(true)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to validate api key, repository returns false, expected false value`() {
        whenever(stubFalAiGenerationRepository.validateApiKey())
            .thenReturn(Single.just(false))

        useCase()
            .test()
            .assertNoErrors()
            .assertValue(false)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to validate api key, repository throws exception, expected error value`() {
        whenever(stubFalAiGenerationRepository.validateApiKey())
            .thenReturn(Single.error(stubException))

        useCase()
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }
}
